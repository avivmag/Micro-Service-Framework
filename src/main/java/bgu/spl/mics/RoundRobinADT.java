package bgu.spl.mics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A synchronized round robin fashioned queue for any purpose
 * @author aviv
 *
 * @param <T>
 */
public class RoundRobinADT <T> {
	private List<T> adt = new ArrayList<T>();
	private int index;
	
    public RoundRobinADT() {
		index = -1;
	}
	/**
	 * Add {@code t} to the adt.
	 * @param t
	 */
	public synchronized void add(T t)
	{
		if(!adt.contains(t))
			adt.add(t);
	}
	/**
	 * Get the next element.
	 * @return T the next element.
	 */
	public synchronized T getNext()
	{
		if(adt.size() != 0)
		{
			index++;
			if(index >= adt.size())
				index = 0;
			
			return adt.get(index);
		}
		else
			return null;
	}
	/**
	 * Remove {@code t} from the adt. 
	 * @param t
	 * @return True if the remove succeeded, false otherwise.
	 */
	public synchronized boolean remove(T t)
	{
		int tPos = adt.indexOf(t);
		boolean succcess = adt.remove(t);
		if(succcess && tPos < index)
			index--;
		
		return succcess;
	}
	/**
	 * @return True if the adt is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return adt.isEmpty();
	}
}
