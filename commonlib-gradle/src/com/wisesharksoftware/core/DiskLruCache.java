package com.wisesharksoftware.core;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//taken from http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
public final class DiskLruCache implements Closeable
{
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TMP = "journal.tmp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = "1";
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    private static final String REMOVE = "REMOVE";
    private static final String READ = "READ";

    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private final File directory;
    private final File journalFile;
    private final File journalFileTmp;
    private final int appVersion;
    private final long maxSize;
    private final int valueCount;
    private long size = 0;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<String, Entry>( 0, 0.75f, true );
    private int redundantOpCount;

    private long nextSequenceNumber = 0;

    private static <T> T[] copyOfRange( T[] original, int start, int end )
    {
        final int originalLength = original.length;
        if( start > end )
        {
            throw new IllegalArgumentException();
        }
        if( start < 0 || start > originalLength )
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        final int resultLength = end - start;
        final int copyLength = Math.min( resultLength, originalLength - start );
        final T[] result = ( T[] )Array
                .newInstance( original.getClass().getComponentType(), resultLength );
        System.arraycopy( original, start, result, 0, copyLength );
        return result;
    }

    public static String readFully( Reader reader ) throws IOException
    {
        try
        {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[ 1024 ];
            int count;
            while( ( count = reader.read( buffer ) ) != -1 )
            {
                writer.write( buffer, 0, count );
            }
            return writer.toString();
        }
        finally
        {
            reader.close();
        }
    }

    public static String readAsciiLine( InputStream in ) throws IOException
    {
        StringBuilder result = new StringBuilder( 80 );
        while( true )
        {
            int c = in.read();
            if( c == -1 )
            {
                throw new EOFException();
            }
            else if( c == '\n' )
            {
                break;
            }

            result.append( ( char )c );
        }
        int length = result.length();
        if( length > 0 && result.charAt( length - 1 ) == '\r' )
        {
            result.setLength( length - 1 );
        }
        return result.toString();
    }

    public static void closeQuietly( Closeable closeable )
    {
        if( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch( RuntimeException rethrown )
            {
                throw rethrown;
            }
            catch( Exception ignored )
            {
            }
        }
    }

    public static void deleteContents( File dir ) throws IOException
    {
        File[] files = dir.listFiles();
        if( files == null )
        {
            throw new IllegalArgumentException( "not a directory: " + dir );
        }
        for( File file : files )
        {
            if( file.isDirectory() )
            {
                deleteContents( file );
            }
            if( !file.delete() )
            {
                throw new IOException( "failed to delete file: " + file );
            }
        }
    }

    private final ExecutorService executorService = new ThreadPoolExecutor( 0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
    private final Callable<Void> cleanupCallable = new Callable<Void>()
    {
        @Override
        public Void call() throws Exception
        {
            synchronized( DiskLruCache.this )
            {
                if( journalWriter == null )
                {
                    return null;
                }
                trimToSize();
                if( journalRebuildRequired() )
                {
                    rebuildJournal();
                    redundantOpCount = 0;
                }
            }
            return null;
        }
    };

    private DiskLruCache( File directory, int appVersion, int valueCount, long maxSize )
    {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File( directory, JOURNAL_FILE );
        this.journalFileTmp = new File( directory, JOURNAL_FILE_TMP );
        this.valueCount = valueCount;
        this.maxSize = maxSize;
    }

    public static DiskLruCache open( File directory, int appVersion, int valueCount, long maxSize ) throws IOException
    {
        if( maxSize <= 0 )
        {
            throw new IllegalArgumentException( "maxSize <= 0" );
        }
        if( valueCount <= 0 )
        {
            throw new IllegalArgumentException( "valueCount <= 0" );
        }

        // prefer to pick up where we left off
        DiskLruCache cache = new DiskLruCache( directory, appVersion, valueCount, maxSize );
        if( cache.journalFile.exists() )
        {
            try
            {
                cache.readJournal();
                cache.processJournal();
                cache.journalWriter = new BufferedWriter( new FileWriter( cache.journalFile, true ), IO_BUFFER_SIZE );
                return cache;
            }
            catch( IOException journalIsCorrupt )
            {
                cache.delete();
            }
        }

        directory.mkdirs();
        cache = new DiskLruCache( directory, appVersion, valueCount, maxSize );
        cache.rebuildJournal();
        return cache;
    }

    private void readJournal() throws IOException
    {
        InputStream in = new BufferedInputStream( new FileInputStream( journalFile ), IO_BUFFER_SIZE );
        try
        {
            String magic = readAsciiLine( in );
            String version = readAsciiLine( in );
            String appVersionString = readAsciiLine( in );
            String valueCountString = readAsciiLine( in );
            String blank = readAsciiLine( in );
            if( !MAGIC.equals( magic )
                || !VERSION_1.equals( version )
                || !Integer.toString( appVersion ).equals( appVersionString )
                || !Integer.toString( valueCount ).equals( valueCountString )
                || !"".equals( blank ) )
            {
                throw new IOException( "unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]" );
            }

            while( true )
            {
                try
                {
                    readJournalLine( readAsciiLine( in ) );
                }
                catch( EOFException endOfJournal )
                {
                    break;
                }
            }
        }
        finally
        {
            closeQuietly( in );
        }
    }

    private void readJournalLine( String line ) throws IOException
    {
        String[] parts = line.split( " " );
        if( parts.length < 2 )
        {
            throw new IOException( "unexpected journal line: " + line );
        }

        String key = parts[ 1 ];
        if( parts[ 0 ].equals( REMOVE ) && parts.length == 2 )
        {
            lruEntries.remove( key );
            return;
        }

        Entry entry = lruEntries.get( key );
        if( entry == null )
        {
            entry = new Entry( key );
            lruEntries.put( key, entry );
        }

        if( parts[ 0 ].equals( CLEAN ) && parts.length == 2 + valueCount )
        {
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths( copyOfRange( parts, 2, parts.length ) );
        }
        else if( parts[ 0 ].equals( DIRTY ) && parts.length == 2 )
        {
            entry.currentEditor = new Editor( entry );
        }
        else if( parts[ 0 ].equals( READ ) && parts.length == 2 )
        {
        }
        else
        {
            throw new IOException( "unexpected journal line: " + line );
        }
    }

    private void processJournal() throws IOException
    {
        deleteIfExists( journalFileTmp );
        for( Iterator<Entry> i = lruEntries.values().iterator(); i.hasNext(); )
        {
            Entry entry = i.next();
            if( entry.currentEditor == null )
            {
                for( int t = 0; t < valueCount; t++ )
                {
                    size += entry.lengths[ t ];
                }
            }
            else
            {
                entry.currentEditor = null;
                for( int t = 0; t < valueCount; t++ )
                {
                    deleteIfExists( entry.getCleanFile( t ) );
                    deleteIfExists( entry.getDirtyFile( t ) );
                }
                i.remove();
            }
        }
    }

    private synchronized void rebuildJournal() throws IOException
    {
        if( journalWriter != null )
        {
            journalWriter.close();
        }

        Writer writer = new BufferedWriter( new FileWriter( journalFileTmp ), IO_BUFFER_SIZE );
        writer.write( MAGIC );
        writer.write( "\n" );
        writer.write( VERSION_1 );
        writer.write( "\n" );
        writer.write( Integer.toString( appVersion ) );
        writer.write( "\n" );
        writer.write( Integer.toString( valueCount ) );
        writer.write( "\n" );
        writer.write( "\n" );

        for( Entry entry : lruEntries.values() )
        {
            if( entry.currentEditor != null )
            {
                writer.write( DIRTY + ' ' + entry.key + '\n' );
            }
            else
            {
                writer.write( CLEAN + ' ' + entry.key + entry.getLengths() + '\n' );
            }
        }

        writer.close();
        journalFileTmp.renameTo( journalFile );
        journalWriter = new BufferedWriter( new FileWriter( journalFile, true ), IO_BUFFER_SIZE );
    }

    private static void deleteIfExists( File file ) throws IOException
    {
        if( file.exists() && !file.delete() )
        {
            throw new IOException();
        }
    }

    public synchronized Snapshot get( String key ) throws IOException
    {
        checkNotClosed();
        validateKey( key );
        Entry entry = lruEntries.get( key );
        if( entry == null )
        {
            return null;
        }

        if( !entry.readable )
        {
            return null;
        }

        InputStream[] ins = new InputStream[ valueCount ];
        try
        {
            for( int i = 0; i < valueCount; i++ )
            {
                ins[ i ] = new FileInputStream( entry.getCleanFile( i ) );
            }
        }
        catch( FileNotFoundException e )
        {
            return null;
        }

        redundantOpCount++;
        journalWriter.append( READ + ' ' + key + '\n' );
        if( journalRebuildRequired() )
        {
            executorService.submit( cleanupCallable );
        }

        return new Snapshot( key, entry.sequenceNumber, ins );
    }

    public Editor edit( String key ) throws IOException
    {
        return edit( key, ANY_SEQUENCE_NUMBER );
    }

    private synchronized Editor edit( String key, long expectedSequenceNumber ) throws IOException
    {
        checkNotClosed();
        validateKey( key );
        Entry entry = lruEntries.get( key );
        if( expectedSequenceNumber != ANY_SEQUENCE_NUMBER
            && ( entry == null || entry.sequenceNumber != expectedSequenceNumber ) )
        {
            return null;
        }
        if( entry == null )
        {
            entry = new Entry( key );
            lruEntries.put( key, entry );
        }
        else if( entry.currentEditor != null )
        {
            return null;
        }

        Editor editor = new Editor( entry );
        entry.currentEditor = editor;

        journalWriter.write( DIRTY + ' ' + key + '\n' );
        journalWriter.flush();
        return editor;
    }

    public File getDirectory()
    {
        return directory;
    }

    public long maxSize()
    {
        return maxSize;
    }

    public synchronized long size()
    {
        return size;
    }

    private synchronized void completeEdit( Editor editor, boolean success ) throws IOException
    {
        Entry entry = editor.entry;
        if( entry.currentEditor != editor )
        {
            throw new IllegalStateException();
        }

        if( success && !entry.readable )
        {
            for( int i = 0; i < valueCount; i++ )
            {
                if( !entry.getDirtyFile( i ).exists() )
                {
                    editor.abort();
                    throw new IllegalStateException( "edit didn't create file " + i );
                }
            }
        }

        for( int i = 0; i < valueCount; i++ )
        {
            File dirty = entry.getDirtyFile( i );
            if( success )
            {
                if( dirty.exists() )
                {
                    File clean = entry.getCleanFile( i );
                    dirty.renameTo( clean );
                    long oldLength = entry.lengths[ i ];
                    long newLength = clean.length();
                    entry.lengths[ i ] = newLength;
                    size = size - oldLength + newLength;
                }
            }
            else
            {
                deleteIfExists( dirty );
            }
        }

        redundantOpCount++;
        entry.currentEditor = null;
        if( entry.readable | success )
        {
            entry.readable = true;
            journalWriter.write( CLEAN + ' ' + entry.key + entry.getLengths() + '\n' );
            if( success )
            {
                entry.sequenceNumber = nextSequenceNumber++;
            }
        }
        else
        {
            lruEntries.remove( entry.key );
            journalWriter.write( REMOVE + ' ' + entry.key + '\n' );
        }

        if( size > maxSize || journalRebuildRequired() )
        {
            executorService.submit( cleanupCallable );
        }
    }

    private boolean journalRebuildRequired()
    {
        final int REDUNDANT_OP_COMPACT_THRESHOLD = 2000;
        return redundantOpCount >= REDUNDANT_OP_COMPACT_THRESHOLD
               && redundantOpCount >= lruEntries.size();
    }

    public synchronized boolean remove( String key ) throws IOException
    {
        checkNotClosed();
        validateKey( key );
        Entry entry = lruEntries.get( key );
        if( entry == null || entry.currentEditor != null )
        {
            return false;
        }

        for( int i = 0; i < valueCount; i++ )
        {
            File file = entry.getCleanFile( i );
            if( !file.delete() )
            {
                throw new IOException( "failed to delete " + file );
            }
            size -= entry.lengths[ i ];
            entry.lengths[ i ] = 0;
        }

        redundantOpCount++;
        journalWriter.append( REMOVE + ' ' + key + '\n' );
        lruEntries.remove( key );

        if( journalRebuildRequired() )
        {
            executorService.submit( cleanupCallable );
        }

        return true;
    }

    public boolean isClosed()
    {
        return journalWriter == null;
    }

    private void checkNotClosed()
    {
        if( journalWriter == null )
        {
            throw new IllegalStateException( "cache is closed" );
        }
    }

    public synchronized void flush() throws IOException
    {
        checkNotClosed();
        trimToSize();
        journalWriter.flush();
    }

    public synchronized void close() throws IOException
    {
        if( journalWriter == null )
        {
            return;
        }
        for( Entry entry : new ArrayList<Entry>( lruEntries.values() ) )
        {
            if( entry.currentEditor != null )
            {
                entry.currentEditor.abort();
            }
        }
        trimToSize();
        journalWriter.close();
        journalWriter = null;
    }

    private void trimToSize() throws IOException
    {
        while( size > maxSize )
        {
            final Map.Entry<String, Entry> toEvict = lruEntries.entrySet().iterator().next();
            remove( toEvict.getKey() );
        }
    }

    public void delete() throws IOException
    {
        close();
        deleteContents( directory );
    }

    private void validateKey( String key )
    {
        if( key.contains( " " ) || key.contains( "\n" ) || key.contains( "\r" ) )
        {
            throw new IllegalArgumentException( "keys must not contain spaces or newlines: \"" + key + "\"" );
        }
    }

    private static String inputStreamToString( InputStream in ) throws IOException
    {
        return readFully( new InputStreamReader( in, UTF_8 ) );
    }

    public final class Snapshot implements Closeable
    {
        private final String key;
        private final long sequenceNumber;
        private final InputStream[] ins;

        private Snapshot( String key, long sequenceNumber, InputStream[] ins )
        {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.ins = ins;
        }

        public Editor edit() throws IOException
        {
            return DiskLruCache.this.edit( key, sequenceNumber );
        }

        public InputStream getInputStream( int index )
        {
            return ins[ index ];
        }

        public String getString( int index ) throws IOException
        {
            return inputStreamToString( getInputStream( index ) );
        }

        @Override
        public void close()
        {
            for( InputStream in : ins )
            {
                closeQuietly( in );
            }
        }
    }

    public final class Editor
    {
        private final Entry entry;
        private boolean hasErrors;

        private Editor( Entry entry )
        {
            this.entry = entry;
        }

        public InputStream newInputStream( int index ) throws IOException
        {
            synchronized( DiskLruCache.this )
            {
                if( entry.currentEditor != this )
                {
                    throw new IllegalStateException();
                }
                if( !entry.readable )
                {
                    return null;
                }
                return new FileInputStream( entry.getCleanFile( index ) );
            }
        }

        public String getString( int index ) throws IOException
        {
            InputStream in = newInputStream( index );
            return in != null ? inputStreamToString( in ) : null;
        }
        
        public OutputStream newOutputStream( int index ) throws IOException
        {
            synchronized( DiskLruCache.this )
            {
                if( entry.currentEditor != this )
                {
                    throw new IllegalStateException();
                }
                return new FaultHidingOutputStream( new FileOutputStream( entry.getDirtyFile( index ) ) );
            }
        }

        public void set( int index, String value ) throws IOException
        {
            Writer writer = null;
            try
            {
                writer = new OutputStreamWriter( newOutputStream( index ), UTF_8 );
                writer.write( value );
            }
            finally
            {
                closeQuietly( writer );
            }
        }

        public void commit() throws IOException
        {
            if( hasErrors )
            {
                completeEdit( this, false );
                remove( entry.key );
            }
            else
            {
                completeEdit( this, true );
            }
        }

        public void abort() throws IOException
        {
            completeEdit( this, false );
        }

        private class FaultHidingOutputStream extends FilterOutputStream
        {
            private FaultHidingOutputStream( OutputStream out )
            {
                super( out );
            }

            @Override
            public void write( int oneByte )
            {
                try
                {
                    out.write( oneByte );
                }
                catch( IOException e )
                {
                    hasErrors = true;
                }
            }

            @Override
            public void write( byte[] buffer, int offset, int length )
            {
                try
                {
                    out.write( buffer, offset, length );
                }
                catch( IOException e )
                {
                    hasErrors = true;
                }
            }

            @Override
            public void close()
            {
                try
                {
                    out.close();
                }
                catch( IOException e )
                {
                    hasErrors = true;
                }
            }

            @Override
            public void flush()
            {
                try
                {
                    out.flush();
                }
                catch( IOException e )
                {
                    hasErrors = true;
                }
            }
        }
    }

    private final class Entry
    {
        private final String key;
        private final long[] lengths;
        private boolean readable;
        private Editor currentEditor;
        private long sequenceNumber;

        private Entry( String key )
        {
            this.key = key;
            this.lengths = new long[ valueCount ];
        }

        public String getLengths() throws IOException
        {
            StringBuilder result = new StringBuilder();
            for( long size : lengths )
            {
                result.append( ' ' ).append( size );
            }
            return result.toString();
        }

        private void setLengths( String[] strings ) throws IOException
        {
            if( strings.length != valueCount )
            {
                throw invalidLengths( strings );
            }

            try
            {
                for( int i = 0; i < strings.length; i++ )
                {
                    lengths[ i ] = Long.parseLong( strings[ i ] );
                }
            }
            catch( NumberFormatException e )
            {
                throw invalidLengths( strings );
            }
        }

        private IOException invalidLengths( String[] strings ) throws IOException
        {
            throw new IOException( "unexpected journal line: " + Arrays.toString( strings ) );
        }

        public File getCleanFile( int i )
        {
            return new File( directory, key + "." + i );
        }

        public File getDirtyFile( int i )
        {
            return new File( directory, key + "." + i + ".tmp" );
        }
    }
}
