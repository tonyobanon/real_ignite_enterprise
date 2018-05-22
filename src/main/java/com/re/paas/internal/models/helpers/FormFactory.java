package com.re.paas.internal.models.helpers;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.core.forms.ColumnSet;
import com.re.paas.internal.core.forms.CompositeEntry;
import com.re.paas.internal.core.forms.InputType;
import com.re.paas.internal.core.forms.Question;
import com.re.paas.internal.core.forms.SimpleEntry;
import com.re.paas.internal.core.pdf.Column;
import com.re.paas.internal.core.pdf.ColumnCollection;
import com.re.paas.internal.core.pdf.Image;
import com.re.paas.internal.core.pdf.InputControl;
import com.re.paas.internal.core.pdf.PDFForm;
import com.re.paas.internal.core.pdf.PDFWriter;
import com.re.paas.internal.core.pdf.Row;
import com.re.paas.internal.core.pdf.Rowset;
import com.re.paas.internal.core.pdf.Section;
import com.re.paas.internal.core.pdf.SizeSpec;
import com.re.paas.internal.core.pdf.Table;
import com.re.paas.internal.core.pdf.TableConfig;
import com.re.paas.internal.core.pdf.TextControl;
import com.re.paas.internal.core.pdf.InputControl.BorderType;
import com.re.paas.internal.core.pdf.InputControl.Size;
import com.re.paas.internal.models.RBModel;
import com.re.paas.internal.utils.Utils;

@BlockerTodo("More space needed for: * Kindly attach a copy of your passport")
public class FormFactory {

	public static File toPDF(String locale, SizeSpec bodySize, SizeSpec headerSize, SizeSpec otherSize, PDFForm sheet) {

		try {

			// Create Temp File
			File tempFile = File.createTempFile(Utils.newRandom(), ".pdf");

			// Generate, write to temp file

			PDFWriter writer = new PDFWriter(tempFile.getAbsolutePath());

			Table table = new Table(new TableConfig(100));

			table

					.withRow(new Row(otherSize)

							.withColumn(new Column(sheet.getSubtitleLeft()).withPercentileWidth(30))

							.withColumn(new Column().withPercentileWidth(45))

							.withColumn(new Column(sheet.getSubtitleRight()).withPercentileWidth(25)));

			table.withRow(new Row(new SizeSpec(3)));

			table

					.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(35))

							.withColumn(sheet.getLogoURL() != null && !sheet.getLogoURL().equals("")
									? new Column(new Image(sheet.getLogoURL())).withPercentileWidth(15)
									: null)

							.withColumn(
									new Column(new TextControl(sheet.getTitle()).forHeader()).withPercentileWidth(40))

							.withColumn(new Column().withPercentileWidth(30)));

			table.withRow(new Row(new SizeSpec(1)));

			for (Section section : sheet.getSections()) {

				table.withRow(new Row(headerSize).withColumn(
						new Column(new TextControl(RBModel.get(locale, section.getTitle().getValue())).forSection())
								.withPercentileWidth(100)));

				if (section.getSummary() != null) {

					table.withRow(new Row(bodySize).withColumn(
							new Column(new TextControl(RBModel.get(locale, section.getSummary().getValue())))
									.withPercentileWidth(100)));
				}

				int position = 1;
				Row currentRow = null;

				List<Question> entries = section.getEntries();

				for (int i = 0; i < entries.size(); i++) {

					Question question = entries.get(i);

					if (position == 1) {

						currentRow = new Row(bodySize);

						ColumnCollection columnCollection = getColumns(locale, sheet.getInputTypePrefixes(), bodySize,
								question, 47);

						if (columnCollection == null) {
							continue;
						}

						if (columnCollection instanceof ColumnSet) {

							ColumnSet columns = (ColumnSet) columnCollection;

							for (Column column : columns.getColumns()) {
								currentRow.withColumn(column);
							}

							currentRow.withColumn(new Column().withPercentileWidth(5));

							position = 2;

							// Since, no more columns exists, we can now append
							// row. To avoid tautology in row append, verify
							// that its
							// not in a single row
							if (i + 1 == entries.size() && !columns.isSingleRow()) {
								table.withRow(currentRow);
							}

							if (columns.isSingleRow()) {

								if (columns.getRowPadding() != null) {
									currentRow.withPadding(columns.getRowPadding());
								}

								if (columns.getRowFontSpec() != null) {
									currentRow.withFontSpec(columns.getRowFontSpec());
								}

								if (columns.getRowComponentSizeSpec() != null) {
									currentRow.withComponentSizeSpec(columns.getRowComponentSizeSpec());
								}

								table.withRow(currentRow);
								currentRow = new Row(bodySize);
								position = 1;
							}

						} else {

							Rowset rows = (Rowset) columnCollection;

							for (Row row : rows.getRows()) {
								table.withRow(row);
							}
						}

					} else {

						ColumnCollection columnCollection = getColumns(locale, sheet.getInputTypePrefixes(), bodySize,
								question, 47);

						if (columnCollection == null) {
							continue;
						}
						if (columnCollection instanceof ColumnSet) {

							ColumnSet columns = (ColumnSet) columnCollection;

							if (columns.isSingleRow()) {

								table.withRow(currentRow);
								currentRow = new Row(bodySize);

								if (columns.getRowPadding() != null) {
									currentRow.withPadding(columns.getRowPadding());
								}

								if (columns.getRowFontSpec() != null) {
									currentRow.withFontSpec(columns.getRowFontSpec());
								}

								if (columns.getRowComponentSizeSpec() != null) {
									currentRow.withComponentSizeSpec(columns.getRowComponentSizeSpec());
								}
							}

							for (Column column : columns.getColumns()) {
								currentRow.withColumn(column);
							}

							table.withRow(currentRow);

							position = 1;

						} else {

							table.withRow(currentRow);

							Rowset rows = (Rowset) columnCollection;

							for (Row row : rows.getRows()) {
								table.withRow(row);
							}
						}
					}
				}

				// Give some extra space
				table.withRow(new Row(bodySize));
			}

			table.commit();

			writer.appendTable(table, false);

			writer.close();

			return tempFile;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ColumnCollection getColumns(String locale, Map<InputType, String> inputTypePrefixes,
			SizeSpec bodySize, Question question, int percentile) {

		if (question instanceof SimpleEntry) {

			SimpleEntry se = (SimpleEntry) question;

			if (!se.getIsVisible()) {
				return null;
			}

			String title = RBModel.get(locale, se.getTitleAsRBRef().getValue());

			switch (se.getInputType()) {

			case TEXT:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new TextControl(se.getTextValue())).withPercentileWidth(percentile * 0.6));

			case AMOUNT:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(inputTypePrefixes.get(InputType.AMOUNT))
								.withPercentileWidth((percentile * 0.6) * 0.15))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.MEDIUM))
								.withPercentileWidth((percentile * 0.6) * 0.85));

			case IMAGE:
				return new ColumnSet().add(new Column("* Kindly attach a copy of your " + title.toLowerCase())
						.withPercentileWidth(percentile).withSingleRow(true)).setSingleRow(true);

			case NUMBER:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));

			case NUMBER_2L:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.MEDIUM))
								.withPercentileWidth(percentile * 0.6));

			case NUMBER_3L:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.MEDIUM))
								.withPercentileWidth(percentile * 0.6));

			case NUMBER_4L:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.MEDIUM))
								.withPercentileWidth(percentile * 0.6));

			case EMAIL:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));

			case PHONE:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));

			case PLAIN:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));

			case SECRET:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));

			case BOOLEAN:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.FULL).withWidth(Size.SMALL))
								.withPercentileWidth(percentile * 0.6));
			case SIGNATURE:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.FULL).withWidth(Size.MEDIUM))
								.withPercentileWidth(percentile * 0.6))
						.setSingleRow(true).setRowComponentSizeSpec(new SizeSpec(9)).setRowPadding(new SizeSpec(8));
			case COUNTRY:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));
			case TERRITORY:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));
			case CITY:
				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));
			default:
				return null;

			}

		} else {
  
			CompositeEntry ce = (CompositeEntry) question;

			if (!ce.getIsVisible()) {
				return null;
			}

			String title = RBModel.get(locale, ce.getTitleAsRBRef().getValue());

			Rowset rowset = new Rowset();
			rowset.withRow(new Row(bodySize).withColumn(new Column(title + ": ")));

			if (ce.getItems().isEmpty()) {

				return new ColumnSet().add(new Column(title + ": ").withPercentileWidth(percentile * 0.4))
						.add(new Column(new InputControl(BorderType.BOTTOM_ONLY).withWidth(Size.LARGE))
								.withPercentileWidth(percentile * 0.6));
			} else {

				// @TODO: Prompt the user to either select one or select multiple,
				// depending on the select type
				ce.getItems().forEach((k, v) -> {
					rowset.withRow(new Row(bodySize).withColumn(new Column().withPercentileWidth(2))
							.withColumn(new Column(new InputControl(BorderType.FULL).withWidth(Size.SMALL))
									.withPercentileWidth(2))
							.withComponentSizeSpec(new SizeSpec(3)).withColumn(new Column().withPercentileWidth(2))
							.withColumn(new Column(k.toString()).withPercentileWidth(85)));
				});

				return rowset;
			}
		}
	}

}
