package com.example.sdnrxtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;

@DataNeo4jTest
public class RelationshipsTest {

	private final static UUID APP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	@Autowired
	Neo4jClient neo4jClient;

	@Autowired
	Neo4jTemplate neo4jTemplate;

	@Test
	void should_save_new_user_and_link_to_application() {

		neo4jClient.query("""
    		CREATE (u:User:Person{uuid:"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", userId:"alice"})
    		CREATE (app:Application{uuid:"00000000-0000-0000-0000-000000000000"})
    		CREATE (app)-[:HAS_USER]->(u)
				""").run();
		Application application = neo4jTemplate.findById(APP_UUID, Application.class).orElseThrow();
		assertThat(application.getUsers()).extracting(User::getUserId).containsOnly("alice");

		User bob = new User(null, "bob", application);
		neo4jTemplate.save(bob);
		// ^^ runs the following statements
		// MERGE (n:`User` {uuid: $__id__}) SET n = $__properties__ RETURN id(n)
		// MATCH (startNode:`User`)<-[rel:`HAS_USER`]-(:`Application`) WHERE startNode.uuid = $fromId DELETE rel
		// MERGE (n:`Application` {uuid: $__id__}) SET n = $__properties__ RETURN id(n)
		// MATCH (startNode:`User`) WHERE startNode.uuid = $fromId MATCH (endNode) WHERE id(endNode) = 36 MERGE (startNode)<-[:`HAS_USER`]-(endNode)
		//    other potential issue ^^ : the internal node id is not bound as a parameter, hurts performance
		// MATCH (startNode:`Application`)-[rel:`HAS_USER`]->(:`User`) WHERE startNode.uuid = $fromId DELETE rel
		// MERGE (n:`User` {uuid: $__id__}) SET n = $__properties__ RETURN id(n)
		// MATCH (startNode:`Application`) WHERE startNode.uuid = $fromId MATCH (endNode) WHERE id(endNode) = 35 MERGE (startNode)-[:`HAS_USER`]->(endNode)
		// MATCH (startNode:`User`)<-[rel:`HAS_USER`]-(:`Application`) WHERE startNode.uuid = $fromId DELETE rel

		Application applicationReloaded = neo4jTemplate.findById(APP_UUID, Application.class).orElseThrow();
		assertThat(applicationReloaded.getUsers()).extracting(User::getUserId).containsOnly("alice", "bob");
	}

	// variant of the above, syncing manually both ends of the association
	// stack overflows
	@Test
	void should_save_new_user_and_link_to_application_with_explicit_sync() {

		neo4jClient.query("""
    		CREATE (u:User:Person{uuid:"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", userId:"alice"})
    		CREATE (app:Application{uuid:"00000000-0000-0000-0000-000000000000"})
    		CREATE (app)-[:HAS_USER]->(u)
				""").run();
		Application application = neo4jTemplate.findById(APP_UUID, Application.class).orElseThrow();
		assertThat(application.getUsers()).extracting(User::getUserId).containsOnly("alice");

		User bob = new User(null, "bob", application);
		application.getUsers().add(bob);
		neo4jTemplate.save(bob);

		Application applicationReloaded = neo4jTemplate.findById(APP_UUID, Application.class).orElseThrow();
		assertThat(applicationReloaded.getUsers()).extracting(User::getUserId).containsOnly("alice", "bob");
	}

}
