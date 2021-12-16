package fr.pantheonsorbonne.miage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class StudentRepository implements Iterable<Student> {

	private String db;
	private java.util.Iterator<Student> currentIterator = null;

	private StudentRepository(String db) {
		this.db = db;
	}

	public static StudentRepository withDB(String db) {
		if (!Files.exists(Paths.get(db))) {
			throw new RuntimeException("failed to find" + Paths.get(db).toAbsolutePath().toString());
		}
		return new StudentRepository(db);
	}

	public static List<String> toReccord(Student stu) {

		return Arrays.asList(stu.getName(), stu.getTitle(), "" + stu.getId());
	}

	public StudentRepository add(Student s) {
		Iterator<Student> previousContent = StudentRepository.withDB(this.db).iterator();
		try (FileWriter writer = new FileWriter(this.db)) {
			CSVPrinter csvFilePrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

			previousContent.forEachRemaining(student -> {
				try {
					csvFilePrinter.printRecord(toReccord(student));
				} catch (IOException e) {
					throw new RuntimeException("failed to update db file");
				}
			});
			csvFilePrinter.printRecord(toReccord(s));
			csvFilePrinter.flush();
			csvFilePrinter.close(true);

		} catch (IOException e) {
			throw new RuntimeException("failed to update db file");
		}
		return this;

	}

	@SuppressWarnings("unchecked")
	@Override
	public java.util.Iterator<Student> iterator() {
		try (FileReader reader = new FileReader(this.db)) {

			CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
			this.currentIterator = parser.getRecords().stream()
					.map(reccord -> new Student(Integer.parseInt(reccord.get(2)), reccord.get(0), reccord.get(1)))
					.map(c -> c).iterator();
			return this.currentIterator;

		} catch (IOException e) {
			Logger.getGlobal().info("IO PB" + e.getMessage());
			return Collections.emptySet().iterator();
		}
	}

}
