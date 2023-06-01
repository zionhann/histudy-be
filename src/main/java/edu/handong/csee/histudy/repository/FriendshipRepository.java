package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship,Long> {
}
