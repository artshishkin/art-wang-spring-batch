package net.shyshkin.study.jpa.repository;

import net.shyshkin.study.jpa.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
