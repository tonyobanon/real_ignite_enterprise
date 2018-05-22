package com.re.paas.internal.core.pdf.invoices;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.re.paas.internal.core.pdf.Column;
import com.re.paas.internal.core.pdf.Font;
import com.re.paas.internal.core.pdf.Image;
import com.re.paas.internal.core.pdf.PDFWriter;
import com.re.paas.internal.core.pdf.Row;
import com.re.paas.internal.core.pdf.SizeSpec;
import com.re.paas.internal.core.pdf.Table;
import com.re.paas.internal.core.pdf.TableConfig;
import com.re.paas.internal.core.pdf.TextControl;
import com.re.paas.internal.core.pdf.XCoordinate;

public class InvoiceFactory {

	public static File toPDF(SizeSpec bodySize, SizeSpec headerSize, Order order) {

		try {

			System.out.println("Generating invoice ..");

			// Create Temp File
			File file = new File("/Users/Tony/Documents/order.pdf");

			// Generate, write to temp file

			PDFWriter writer = new PDFWriter(file.getAbsolutePath());

			
			//Add company address info
			
			Table companyAddressSection = new Table(new TableConfig(30));
			companyAddressSection
				.withRow(new Row(headerSize)
						.withColumn(new Column(new Image(order.getCompanyLogo().toString())).withPercentileWidth(20))
						.withColumn(new Column("[" + order.getCompanyName() + "]").withPercentileWidth(80)))
				.withRow(new Row(bodySize))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCompanyAddress())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCompanyCity())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCompanyState() + " " + order.getCompanyZIP())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCompanyPhone())))
				.withRow(new Row(bodySize).withColumn(new Column("Fax: " + order.getCompanyFax())))
			;
			companyAddressSection.commit();
			writer.appendTable(companyAddressSection, false);
			
			
			// Reset Y Coordinates
			writer.resetY();
			
			
			//Add Invoice Id and Customer Id
			
			Table DateSection = new Table(new TableConfig());
			DateSection
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(80)).withColumn(new Column(new TextControl("INVOICE").withFont(Font.HELVETICA_BOLD_OBLIQUE).setFontSize(12))))
			.withRow(new Row(bodySize))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(80)).withColumn(new Column("Date: " + order.getDateCreated())))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(80)).withColumn(new Column("Invoice #: " + order.getOrderId())))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(80)).withColumn(new Column("Customer Id: " + order.getCustomerId())))
			.withRow(new Row(bodySize))
			.withRow(new Row(bodySize))
			;
			DateSection.commit();
			writer.appendTable(DateSection, false);

			
			
			//Add Billing Address
			
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 2);
			writer.drawRect(new Color(255, 229, 204), PDFWriter.PAGE_WIDTH / 3f, PDFWriter.DEFAULT_XS_PADDING, true);
			writer.writeText(new XCoordinate(PDFWriter.DEFAULT_BORDER_X, PDFWriter.PAGE_WIDTH), new SizeSpec(4), new TextControl("BILL TO"));
			
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 2);
			writer.drawRect(new Color(255, 229, 204), PDFWriter.PAGE_WIDTH / 3f, PDFWriter.DEFAULT_XS_PADDING, true);			
			
			Table billingAddressSection = new Table(new TableConfig());
			billingAddressSection
				.withRow(new Row(bodySize).withColumn(new Column( order.getCustomerName())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCustomerAddress())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCustomerCity())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCustomerState() + " " + order.getCustomerZIP())))
				.withRow(new Row(bodySize).withColumn(new Column(order.getCustomerPhone())))
			;
			billingAddressSection.commit();
			writer.appendTable(billingAddressSection, false);
			
			
			
			//Add Order Items
			
			float borderStartY = 0.0f;
			float borderEndY = 0.0f;
			
			//Header
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 2);
			
			borderStartY = writer.currentY();
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING);
			//writer.drawRect(Color.LIGHT_GRAY, PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true);
			
			Table orderItemsHeaders = new Table(new TableConfig());
			orderItemsHeaders
				.withRow(new Row(bodySize)
						.withColumn(new Column().withPercentileWidth(5))
						.withColumn(new Column("NAME").withPercentileWidth(25f))
						.withColumn(new Column("DESCRIPTION").withPercentileWidth(50f))
						.withColumn(new Column("PRICE").withPercentileWidth(20f))
						);
			orderItemsHeaders.commit();
			writer.appendTable(orderItemsHeaders, false);
			
			writer.drawRect(Color.LIGHT_GRAY, PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true);			
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 2);	
			
			
			//Body
			
			List<OrderItem> products = order.getProducts();
			
			for(int i = 0; i < products.size(); i ++) {

				try{
					
					Table orderItemsBody = new Table(new TableConfig());
				
					orderItemsBody.withRow(new Row(bodySize)
							.withColumn(new Column().withPercentileWidth(5))
						.withColumn(new Column(products.get(i).getName()).withPercentileWidth(25f))
						.withColumn(new Column(products.get(i).getDescription()).withPercentileWidth(50f))
						.withColumn(new Column("$ " + products.get(i).getPrice().toString()).withPercentileWidth(20f))
						);
				
					orderItemsBody.commit();
					writer.appendTable(orderItemsBody, false);
				
					if(i < products.size() -1 ){
						//writer.nextY(PDFWriter.DEFAULT_XS_PADDING);
						writer.drawRect(new Color(204, 229, 255), PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true, true, false);
					}
					
				} catch (IOException e) {
					try {
						writer.close();
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
					throw new RuntimeException(e);
				}
				
			}
			
			borderEndY = writer.currentY();
			
			
			//Draw Table Outline
			
			writer.drawRect(Color.lightGray, PDFWriter.DEFAULT_BORDER_X, borderStartY, PDFWriter.PAGE_WIDTH, borderEndY - borderStartY, false, true, false);			
			
			
			
			//Add separator
			
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 5);
			writer.drawRect(Color.LIGHT_GRAY, PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true);
			
			//Add Total Prices
			
			Table pricesSection = new Table(new TableConfig());
			pricesSection
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column("Subtotal").withPercentileWidth(20)).withColumn(new Column("$ " + order.getSubtotal()).withPercentileWidth(20)))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column("Taxable").withPercentileWidth(20)).withColumn(new Column("$ " + order.getTaxable()).withPercentileWidth(20)))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column("Tax rate").withPercentileWidth(20)).withColumn(new Column("$ " + order.getTaxRate()).withPercentileWidth(20)))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column("Tax due").withPercentileWidth(20)).withColumn(new Column("$ " + order.getTaxDue()).withPercentileWidth(20)))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column("Other").withPercentileWidth(20)).withColumn(new Column("$ " + order.getOther()).withPercentileWidth(20)))
			
			;
			pricesSection.commit();
			writer.appendTable(pricesSection, false);

			
			//Add separator
			
			writer.nextY(PDFWriter.DEFAULT_XS_PADDING);
			writer.drawRect(Color.LIGHT_GRAY, PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true, true, false);
			
			Table totalPriceSection = new Table(new TableConfig());
			totalPriceSection
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(60)).withColumn(new Column(new TextControl("TOTAL DUE").setFont(Font.TIMES_BOLD)).withPercentileWidth(20)).withColumn(new Column("$ " + order.getTotal()).withPercentileWidth(20)))			
			;
			totalPriceSection.commit();
			writer.appendTable(totalPriceSection, false);
			
			writer.drawRect(Color.LIGHT_GRAY, PDFWriter.PAGE_WIDTH, PDFWriter.DEFAULT_XS_PADDING, true);
			writer.nextY(PDFWriter.DEFAULT_XS_PADDING);
			
			
			//Check payment information
			
			Table totalPriceExtrasSection = new Table(new TableConfig());
			totalPriceExtrasSection
			.withRow(new Row(bodySize))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(65)).withColumn(new Column(new TextControl("Make all checks payable to").setFont(Font.COURIER_OBLIQUE)).withPercentileWidth(35)))			
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(65)).withColumn(new Column(new TextControl(order.getCompanyName()).setFont(Font.COURIER_OBLIQUE)).withPercentileWidth(35)))			
			;
			totalPriceExtrasSection.commit();
			writer.appendTable(totalPriceExtrasSection, false);
	
			
			
			
			writer.nextY(PDFWriter.DEFAULT_SMALL_PADDING * 12);
			
			
			// Footer Section
	
			Table footerSection = new Table(new TableConfig());
			footerSection
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(25)).withColumn(new Column(new TextControl("If you have any questions about this invoice, please contact").setFont(Font.HELVETICA_OBLIQUE)).withPercentileWidth(50)).withColumn(new Column().withPercentileWidth(25)))
			.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(37)).withColumn(new Column(new TextControl(order.getCompanyEmail()).setFont(Font.HELVETICA_OBLIQUE))).withColumn(new Column()))
			;
			footerSection.commit();
			writer.appendTable(footerSection, false);
	
			
			
			
			writer.close();

			System.out.println("PDF Generation was successful");

			return file;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error occured during PDF Generation: " + e.getMessage());
			// return null;
		}
	}

}
