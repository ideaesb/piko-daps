package org.ideademo.daps.pages.pdf;


import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.StreamResponse;


public class Index {
	@InjectPage
	private org.ideademo.daps.pages.Index index;
	
	public StreamResponse onActivate()
    {
		return index.onSelectedFromPdf();
    }
}
