echo off 
set PATH=.\jre\bin;
set CLASSPATH=.;.\lib\*
start javaw core.RenjuGame


goto start
      1. ʹ��"java core.RenjuGame"��������ֱ������������ڲ��˳�
	  2. Bat�ű������û����룬����Ϊ��������main�������������£�
		echo ���������(�����Ƕ�������磺88 9999)
		set /p input_source=
		java com.FlowForward %input_source%
		
		�ڱ����������߼�һ��%��,��%name%.��CMD�ڶԶ�ȡ���ǵ�����
		������и�ʽƥ���ʱ��,�ͻᷢ��name����ַ����߼���%��,��
		�������������ͨ�ַ�����,���ǻ��������һ����������,������
		��name!Ȼ��CMD�ͻ��ҵ���������Ӧ��ֵ,�ñ�������ֵ�滻����
		����������(name),(���������������ֵ,�ͷ��ؿ�ֵ).

     :start




