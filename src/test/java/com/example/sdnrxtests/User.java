package com.example.sdnrxtests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.GeneratedValue.UUIDGenerator;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;

@Node
@Value
public class User {

	@Id @GeneratedValue(UUIDGenerator.class)
	UUID uuid;

	@NonNull
	String userId;

	@Relationship(type = "HAS_USER", direction = Direction.INCOMING)
	Application application;


}
