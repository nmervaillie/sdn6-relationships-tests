package com.example.sdnrxtests;

import java.util.Collection;
import java.util.UUID;
import lombok.Value;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.GeneratedValue.UUIDGenerator;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
@Value
public class Application {

	@Id @GeneratedValue(UUIDGenerator.class)
	UUID uuid;

	@Relationship(type = "HAS_USER")
	Collection<User> users;
}
