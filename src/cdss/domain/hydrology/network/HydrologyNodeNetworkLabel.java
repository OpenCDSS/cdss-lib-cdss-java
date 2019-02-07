// HydrologyNodeNetworkLabel - simple class to store network label data

/* NoticeStart

CDSS Java Library
CDSS Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package cdss.domain.hydrology.network;

/**
Simple class to store network label data.
*/
public class HydrologyNodeNetworkLabel
{

private double __size;
private double __x;
private double __y;
private int	__flag;
private String __text;

public HydrologyNodeNetworkLabel ( double x, double y, double size, int flag, String text )
{
	__x = x;
	__y = y;
	__size = size;
	__flag = flag;
	__text = text;
}

}
