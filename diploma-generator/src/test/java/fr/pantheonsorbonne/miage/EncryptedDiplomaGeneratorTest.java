package fr.pantheonsorbonne.miage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class EncryptedDiplomaGeneratorTest extends DiplomaGeneratorTest {

	private final class EncryptedPdfReader extends PdfReader {
		private EncryptedPdfReader(String filename, String password) throws IOException {
			super(filename);
			this.password = password.getBytes();

		}

		public void setEncrypted(boolean encrypted) {
			this.encrypted = encrypted;
		}
	}

	@Test
	public void testEncryptedPdfTest() throws IOException, DocumentException {

		Student stu = new Student(1, "Nicolas", "", "nico");
		DiplomaGenerator generator = new MiageDiplomaGenerator(stu, DiplomaGeneratorTest.currentDate);
		AbstractDiplomaGenerator encryptedGenerator = new EncryptedDiplomaGeneratorDecorator(generator, "abc");
		FileGenerator<AbstractDiplomaGenerator> adapter = new DiplomaFileAdapter(encryptedGenerator);

		Path tempFileEncrypted = Files.createTempFile("prefix", ".pdf");
		Path tempFileDecrypted = Files.createTempFile("prefix", ".pdf");

		adapter.generateFile(tempFileEncrypted.toString());

		PdfReader reader = new PdfReader(tempFileEncrypted.toString(), "abc".getBytes());

		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(tempFileDecrypted.toString()));
		stamper.close();
		reader.close();

		// write the bytes of an image version of the generated pdf diploma in this
		// OutputStream
		ByteArrayOutputStream generatedImageData = new ByteArrayOutputStream();

		System.out.println(tempFileDecrypted);

		// write the bytes of an image version of a reference diploma
		ByteArrayOutputStream referenceImageData = new ByteArrayOutputStream();

		writePDFImageRasterBytes(tempFileDecrypted.toFile(), generatedImageData);
		writePDFImageRasterBytes(new File("src/test/resources/nicolas.pdf"), referenceImageData);

		assertArrayEquals(referenceImageData.toByteArray(), generatedImageData.toByteArray());

		stamper.close();
		reader.close();

	}
}
