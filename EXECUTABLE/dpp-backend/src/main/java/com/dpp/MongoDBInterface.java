package com.dpp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface MongoDBInterface extends MongoRepository<MongoDppTemplate, String>{

    @Query("{ '_id': ?0 }") // Finds the shell by its ID
    @Update("{ '$push': { 'dpps': ?1 } }") // Appends the new DPP to the array
    void appendDppToShell(String shellId, MongoDppTemplate dppContent);

}
