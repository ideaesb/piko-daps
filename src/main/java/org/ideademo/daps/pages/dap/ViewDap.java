package org.ideademo.daps.pages.dap;


import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;

import org.ideademo.daps.entities.Dap;


public class ViewDap
{
	
  @PageActivationContext 
  @Property
  private Dap entity;
  
  
  void onPrepareForRender()  {if(this.entity == null){this.entity = new Dap();}}
  void onPrepareForSubmit()  {if(this.entity == null){this.entity = new Dap();}}
}
