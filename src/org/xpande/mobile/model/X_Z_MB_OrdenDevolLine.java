/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.xpande.mobile.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for Z_MB_OrdenDevolLine
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0 - $Id$ */
public class X_Z_MB_OrdenDevolLine extends PO implements I_Z_MB_OrdenDevolLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180103L;

    /** Standard Constructor */
    public X_Z_MB_OrdenDevolLine (Properties ctx, int Z_MB_OrdenDevolLine_ID, String trxName)
    {
      super (ctx, Z_MB_OrdenDevolLine_ID, trxName);
      /** if (Z_MB_OrdenDevolLine_ID == 0)
        {
			setM_Product_ID (0);
			setQtyEntered (Env.ZERO);
			setZ_MB_OrdenDevol_ID (0);
			setZ_MB_OrdenDevolLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_Z_MB_OrdenDevolLine (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_Z_MB_OrdenDevolLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_C_UOM getC_UOM() throws RuntimeException
    {
		return (I_C_UOM)MTable.get(getCtx(), I_C_UOM.Table_Name)
			.getPO(getC_UOM_ID(), get_TrxName());	}

	/** Set UOM.
		@param C_UOM_ID 
		Unit of Measure
	  */
	public void setC_UOM_ID (int C_UOM_ID)
	{
		if (C_UOM_ID < 1) 
			set_Value (COLUMNNAME_C_UOM_ID, null);
		else 
			set_Value (COLUMNNAME_C_UOM_ID, Integer.valueOf(C_UOM_ID));
	}

	/** Get UOM.
		@return Unit of Measure
	  */
	public int getC_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_M_Product getM_Product() throws RuntimeException
    {
		return (I_M_Product)MTable.get(getCtx(), I_M_Product.Table_Name)
			.getPO(getM_Product_ID(), get_TrxName());	}

	/** Set Product.
		@param M_Product_ID 
		Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Quantity.
		@param QtyEntered 
		The Quantity Entered is based on the selected UoM
	  */
	public void setQtyEntered (BigDecimal QtyEntered)
	{
		set_Value (COLUMNNAME_QtyEntered, QtyEntered);
	}

	/** Get Quantity.
		@return The Quantity Entered is based on the selected UoM
	  */
	public BigDecimal getQtyEntered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyEntered);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set UPC/EAN.
		@param UPC 
		Bar Code (Universal Product Code or its superset European Article Number)
	  */
	public void setUPC (String UPC)
	{
		set_Value (COLUMNNAME_UPC, UPC);
	}

	/** Get UPC/EAN.
		@return Bar Code (Universal Product Code or its superset European Article Number)
	  */
	public String getUPC () 
	{
		return (String)get_Value(COLUMNNAME_UPC);
	}

	public org.xpande.mobile.model.I_Z_MB_OrdenDevol getZ_MB_OrdenDevol() throws RuntimeException
    {
		return (org.xpande.mobile.model.I_Z_MB_OrdenDevol)MTable.get(getCtx(), org.xpande.mobile.model.I_Z_MB_OrdenDevol.Table_Name)
			.getPO(getZ_MB_OrdenDevol_ID(), get_TrxName());	}

	/** Set Z_MB_OrdenDevol ID.
		@param Z_MB_OrdenDevol_ID Z_MB_OrdenDevol ID	  */
	public void setZ_MB_OrdenDevol_ID (int Z_MB_OrdenDevol_ID)
	{
		if (Z_MB_OrdenDevol_ID < 1) 
			set_Value (COLUMNNAME_Z_MB_OrdenDevol_ID, null);
		else 
			set_Value (COLUMNNAME_Z_MB_OrdenDevol_ID, Integer.valueOf(Z_MB_OrdenDevol_ID));
	}

	/** Get Z_MB_OrdenDevol ID.
		@return Z_MB_OrdenDevol ID	  */
	public int getZ_MB_OrdenDevol_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_OrdenDevol_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Z_MB_OrdenDevolLine ID.
		@param Z_MB_OrdenDevolLine_ID Z_MB_OrdenDevolLine ID	  */
	public void setZ_MB_OrdenDevolLine_ID (int Z_MB_OrdenDevolLine_ID)
	{
		if (Z_MB_OrdenDevolLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_Z_MB_OrdenDevolLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Z_MB_OrdenDevolLine_ID, Integer.valueOf(Z_MB_OrdenDevolLine_ID));
	}

	/** Get Z_MB_OrdenDevolLine ID.
		@return Z_MB_OrdenDevolLine ID	  */
	public int getZ_MB_OrdenDevolLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_MB_OrdenDevolLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}