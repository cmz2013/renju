echo off 
set PATH=.\jre\bin;
set CLASSPATH=.;.\lib\*
start javaw core.RenjuGame


goto start
      1. 使用"java core.RenjuGame"启动程序，直到程序结束窗口才退出
	  2. Bat脚本接受用户输入，并作为参数传给main方法，代码如下：
		echo 请输入参数(可以是多个，例如：88 9999)
		set /p input_source=
		java com.FlowForward %input_source%
		
		在变量名字两边加一个%号,如%name%.当CMD在对读取我们的整行
		命令进行格式匹配的时候,就会发现name这个字符两边加了%号,就
		不会把他当作普通字符处理,而是会把他当作一个变量处理,变量名
		叫name!然后CMD就会找到变量名对应的值,用变量名的值替换掉这
		个变量名字(name),(如果变量名不存在值,就返回空值).

     :start




