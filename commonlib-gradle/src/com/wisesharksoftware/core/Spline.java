package com.wisesharksoftware.core;

public class Spline
{
    private static double[] secondDerivative( int[][] points )
    {
        int n = points.length;

        // build the tridiagonal system 
        // (assume 0 boundary conditions: y2[0]=y2[-1]=0) 
        double[][] matrix = new double[ n ][ 3 ];
        double[] result = new double[ n ];
        matrix[ 0 ][ 1 ] = 1;
        for( int i = 1; i < n - 1; ++i )
        {
            matrix[ i ][ 0 ] = ( double )( points[ i ][ 0 ] - points[ i - 1 ][ 0 ] ) / 6;
            matrix[ i ][ 1 ] = ( double )( points[ i + 1 ][ 0 ] - points[ i - 1 ][ 0 ] ) / 3;
            result[ i ] = ( double )( points[ i + 1 ][ 1 ] - points[ i ][ 1 ] ) /
                          ( points[ i + 1 ][ 0 ] - points[ i ][ 0 ] ) -
                          ( double )( points[ i ][ 1 ] - points[ i - 1 ][ 1 ] ) /
                          ( points[ i ][ 0 ] - points[ i - 1 ][ 0 ] );
        }
        matrix[ n - 1 ][ 1 ] = 1;

        // solving pass1 (up->down)
        for( int i = 1; i < n; ++i )
        {
            double k = matrix[ i ][ 0 ] / matrix[ i - 1 ][ 1 ];
            matrix[ i ][ 1 ] -= k * matrix[ i - 1 ][ 2 ];
            matrix[ i ][ 0 ] = 0;
            result[ i ] -= k * result[ i - 1 ];
        }
        // solving pass2 (down->up)
        for( int i = n - 2; i >= 0; --i )
        {
            double k = matrix[ i ][ 2 ] / matrix[ i + 1 ][ 1 ];
            matrix[ i ][ 1 ] -= k * matrix[ i + 1 ][ 0 ];
            matrix[ i ][ 2 ] = 0;
            result[ i ] -= k * result[ i + 1 ];
        }

        // return second derivative value for each point P
        double[] y2 = new double[ n ];
        for( int i = 0; i < n; ++i )
        {
            y2[ i ] = result[ i ] / matrix[ i ][ 1 ];
        }
        return y2;
    }

    private static int fitColorComponent( double d )
    {
        if( d < 0 )
        {
            return 0;
        }
        if( d > 255 )
        {
            return 255;
        }
        return ( int )d;
    }

    public static int[] getSpline( int[][] key_points )
    {
        int[] result = new int[ 256 ];
        double[] sd = secondDerivative( key_points );
        for( int i = 0; i < key_points.length - 1; ++i )
        {
            int[] cur = key_points[ i ];
            int[] next = key_points[ i + 1 ];
            for( int x = cur[ 0 ]; x < next[ 0 ]; ++x )
            {
                double t = ( double )( x - cur[ 0 ] ) /
                           ( next[ 0 ] - cur[ 0 ] );
                double a = 1 - t;
                double b = t;
                double h = next[ 0 ] - cur[ 0 ];
                double y = a * cur[ 1 ] +
                           b * next[ 1 ] +
                           ( h * h / 6 ) *
                           (
                                   ( a * a * a - a ) * sd[ i ] +
                                   ( b * b * b - b ) * sd[ i + 1 ]
                           );
                result[ x ] = fitColorComponent( y );
            }
        }
        result[ 255 ] = key_points[ key_points.length - 1 ][ 1 ];
        return result;
    }
}
