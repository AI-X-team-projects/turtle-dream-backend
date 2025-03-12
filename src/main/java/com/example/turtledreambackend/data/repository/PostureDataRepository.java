package com.example.turtledreambackend.data.repository;

import com.example.turtledreambackend.data.entity.PostureData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostureDataRepository extends MongoRepository<PostureData, String> {
	List<PostureData> findByUserId(String userId);
}
