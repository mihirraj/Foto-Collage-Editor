<?php
	include('dbinfo.php');
	mysql_connect($host,$user,$pwd);
	mysql_select_db($database);// or die('!');
	function randomAlphaNum($length){
		$rangeMin = pow(36, $length-1);
		$rangeMax = pow(36, $length)-1;
		$base10Rand = mt_rand($rangeMin, $rangeMax);
		$newRand = base_convert($base10Rand, 10, 36);
		return $newRand;
	}
	//$pin=randomAlphaNum(5);
	//$query='INSERT INTO users VALUES("'.$_GET['name'].'","'.$_GET['email'].'","","","","","","")';

        $email ='SELECT email FROM users WHERE email = "'.$_GET['email'].'"';
	$email1 = mysql_query($email);
        if(mysql_num_rows($email1) == 0)
        {
           $query= 'insert into users (name,email,password,smvalue) values ("'.$_GET['name'].'","'.$_GET['email'].'","'.$_GET['password'].'","'.$_GET['smvalue'].'")';
	
	   mysql_query($query);

           echo 1;
        }
        else
        {
          echo 0;
        }
	
	
	//if($result)
	//{
	
	//$query='SELECT pin FROM users WHERE email="'.$_GET['email'].'"';
	//$result = mysql_query($query);
	//$db_field = mysql_fetch_array($result);
	//echo $db_field['pin'];
	
	//}
		//echo 'ok done';
	//else
	//die("errr   ". mysql_error())
		//echo '!';
	//mysql_close();
	
?>
