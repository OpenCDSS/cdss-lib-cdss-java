package cdss.domain.hydrology.network;

/**
Simple class to store label data.
*/
public class Label {

		private double __size;
		private double __x;
		private double __y;
		private int	__flag;
		private String __text;
		
		public Label ( double x, double y, double size, int flag, String text )
		{
			__x = x;
			__y = y;
			__size = size;
			__flag = flag;
			__text = text;
		}
}
