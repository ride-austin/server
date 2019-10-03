DELIMITER $$

DROP FUNCTION IF EXISTS `calculate_rating_average`$$

CREATE FUNCTION `calculate_rating_average` (`default_rating` NUMERIC(3,2), `minimum_rating_threshold` INT, `limit` INT, `avatar_id` LONG)
RETURNS NUMERIC(3,2)
NOT DETERMINISTIC
BEGIN
	DECLARE `rating_average` NUMERIC(3,2) ;
    DECLARE `total` NUMERIC(10);
    SET `total` =
		(   SELECT count(*)
			  FROM rating_updates
			 WHERE rated_avatar_id = `avatar_id`
		);

    IF(`total` < `minimum_rating_threshold`) THEN SET `rating_average` = `default_rating`;
    ELSE
		SET `rating_average` =
			( SELECT AVG(rating) as `rating_average`
				FROM (
						SELECT `rating`
						  FROM `rating_updates`
						 WHERE `rated_avatar_id` = `avatar_id`
					  ORDER BY `created_date` DESC, `id` DESC
						 LIMIT `limit`
					 ) AS `inner_ratings`
			);
	END IF;
	RETURN `rating_average`;
END$$

DELIMITER ;