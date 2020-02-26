package com.test.restapi.dbinit;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.test.restapi.entity.jpa.security.Person;
import com.test.restapi.repository.jpa.security.PersonRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserInitializer {
	@Autowired
	public UserInitializer(PersonRepository repository) throws Exception {

		if (repository.count() != 0) {
			return;
		}

		List<Person> people = readUsers();
		log.info("Importing {} users into JPA store…", people.size());
		repository.saveAll(people);
		log.info("Successfully imported {} users.", repository.count());
	}

	private List<Person> readUsers() throws Exception {
		ClassPathResource resource = new ClassPathResource("people.csv");

		Scanner scanner = new Scanner(resource.getInputStream(), "UTF-8");
		String line = scanner.nextLine();
		scanner.close();

		FlatFileItemReader<Person> itemReader = new FlatFileItemReader<Person>();
		itemReader.setResource(resource);

		// DelimitedLineTokenizer defaults to comma as its delimiter
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames(line.split(","));
		tokenizer.setStrict(false);

		DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<Person>();

		lineMapper.setFieldSetMapper(fields -> {
			Person person = new Person();
			person.setFirstName(fields.readString("firstName"));
			person.setLastName(fields.readString("lastName"));
			person.setEnabled(fields.readBoolean("enabled"));
			person.setUsername(fields.readString("username"));
			person.setPassword(fields.readString("password"));
			person.setTitle(fields.readString("title"));

			return person;
		});

		lineMapper.setLineTokenizer(tokenizer);
		itemReader.setLineMapper(lineMapper);
		itemReader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
		itemReader.setLinesToSkip(1);
		itemReader.open(new ExecutionContext());

		List<Person> users = new ArrayList<>();
		Person user = null;

		do {

			user = itemReader.read();

			if (user != null) {
				users.add(user);
			}

		} while (user != null);

		return users;
	}

}
