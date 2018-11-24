package fr.pantheonsorbonne.miage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.junit.jupiter.api.Test;

import com.google.common.io.ByteStreams;

public class DiplomaGeneratorTest {

	static protected Date currentDate;
	{
		try {
			currentDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse("11/23/2018 17:00:00");
		} catch (ParseException e) {
			throw new RuntimeException("failed to compute test date", e);
		}
	}

	@Test
	void compareGeneratedDiploma() {

		try {

			Student stu = new Student(0, "Nicolas", "","nico");

			File generatedFileTarget = generateDiplomaForStudent(stu, currentDate);

			// write the bytes of an image version of the generated pdf diploma in this
			// OutputStream
			ByteArrayOutputStream generatedImageData = new ByteArrayOutputStream();
			writePDFImageRasterBytes(generatedFileTarget, generatedImageData);

			// write the bytes of an image version of a reference diploma
			ByteArrayOutputStream referenceImageData = new ByteArrayOutputStream();
			writePDFImageRasterBytes(new File("src/test/resources/nicolas.pdf"), referenceImageData);

			// check that the content is the same
			assertArrayEquals(referenceImageData.toByteArray(), generatedImageData.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	protected void writePDFImageRasterBytes(File generatedFileTarget, OutputStream generatedImageData)
			throws IOException, InvalidPasswordException, FileNotFoundException {
		BufferedImage genetatedbim = new PDFRenderer(PDDocument.load(new File(generatedFileTarget.getPath())))
				.renderImage(0);
		File generatedImage = Files.createTempFile("prefix_", ".bmp").toFile();
		System.out.println(generatedImage);
		ImageIOUtil.writeImage(genetatedbim, generatedImage.getPath(), 20);
		FileInputStream generatedImageReader = new FileInputStream(generatedImage);
		ByteStreams.copy(generatedImageReader, generatedImageData);
	}

	protected File generateDiplomaForStudent(Student stu, Date date) throws IOException, FileNotFoundException {
		ByteArrayOutputStream generatedFileContent = new ByteArrayOutputStream();
		File generatedFileTarget = Files.createTempFile("prefix_", "_suffic").toFile();
		MiageDiplomaGenerator generator = new MiageDiplomaGenerator(stu, date);
		new DiplomaFileAdapter(generator).generateFile(generatedFileTarget.getPath());
		FileInputStream generatedFileReader = new FileInputStream(generatedFileTarget);
		ByteStreams.copy(generatedFileReader, generatedFileContent);
		return generatedFileTarget;
	}

}