// $Id: FlipMap.java,v 1.2 2002-05-29 20:32:39 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to flip (invert) the
 * indexing along a particular dimension.
 * Maps {0, 1, ..., N-1} to {N-1, N-2, ..., 0} where
 * N is the length of the dimension.
 *
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:39 $
 */
public class
FlipMap
		extends ConcreteIndexMap
{
	/**
	 * Create an IndexMap which flips the indexing
	 * for a particular dimension.
	 *
	 * @param position the dimension number where the index
	 * is to be flipped.
	 */
	public
	FlipMap(int position)
	{
		init(new IMap());
		position_ = position;
	}

	/**
	 * Create an IndexMap which flips the indexing
	 * for a particular dimension and is functionally composed
	 * with another IndexMap.
	 *
	 * @param prev ConcreteIndexMap to be composed with this.
	 * @param position the dimension number where the index
	 * is to be flipped.
	 */
	public
	FlipMap(ConcreteIndexMap prev, int position)
	{
		link(prev, new IMap());
		position_ = position;
	}

	private class
	IMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			final int value = super.get(key);
			if(key == position_)
				return (lengthsMap_.get(key) -1 - value);
			// else
			return value;
		}
	}

	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int position_;

 /* Begin Test */
	public static void
	main(String[] args)
	{
		final int [] shape = {48, 64};
		MultiArrayImpl delegate =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(delegate.storage,
					ii, ii);

		}
		IndexMap im = new FlipMap(1);
		MultiArray ma = new MultiArrayProxy(delegate, im);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0]
					 + " " + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {0, 0}));
		}
		catch (java.io.IOException ee) {}
	}
 /* Test output java ucar.multiarray.FlipMap
Rank  2
Shape { 48 64 }
63
  */
 /* End Test */
}
