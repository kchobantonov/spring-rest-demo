package com.test.restapi;

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

import com.test.restapi.data.jpa.User;
import com.test.restapi.repository.jpa.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataInitializer {
	@Autowired
	public DataInitializer(UserRepository repository) throws Exception {

		if (repository.count() != 0) {
			return;
		}

		List<User> users = readUsers();
		log.info("Importing {} users into JPA store…", users.size());
		repository.saveAll(users);
		log.info("Successfully imported {} users.", repository.count());
	}

	private List<User> readUsers() throws Exception {
		ClassPathResource resource = new ClassPathResource("users.csv");

		Scanner scanner = new Scanner(resource.getInputStream());
		String line = scanner.nextLine();
		scanner.close();

		FlatFileItemReader<User> itemReader = new FlatFileItemReader<User>();
		itemReader.setResource(resource);

		// DelimitedLineTokenizer defaults to comma as its delimiter
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames(line.split(","));
		tokenizer.setStrict(false);

		DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();

		lineMapper.setFieldSetMapper(fields -> {
			return User.builder().username(fields.readString("UserName")).password(fields.readString("Password"))
					.enabled(fields.readBoolean("Enabled")).build();
		});

		lineMapper.setLineTokenizer(tokenizer);
		itemReader.setLineMapper(lineMapper);
		itemReader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
		itemReader.setLinesToSkip(1);
		itemReader.open(new ExecutionContext());

		List<User> users = new ArrayList<>();
		User user = null;

		do {

			user = itemReader.read();

			if (user != null) {
				users.add(user);
			}

		} while (user != null);

		return users;
	}

}
