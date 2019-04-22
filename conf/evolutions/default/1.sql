-- noinspection SqlNoDataSourceInspectionForFile

-- User schema

-- !Ups

create table `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `nickname` VARCHAR(50) NOT NULL,
   UNIQUE KEY unique_nickname (nickname)
);

create table `game` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `player_id` BIGINT NOT NULL,
  `row_count` int(11) UNSIGNED NOT NULL,
  `column_count` int(11) UNSIGNED NOT NULL,
  `mine_count` int(11) UNSIGNED NOT NULL,
  `board` TEXT NOT NULL,
  `total_time` int(11) UNSIGNED NOT NULL,
  `created_at` DATETIME NOT NULL,
  `latest_interaction_at` DATETIME NOT NULL,
  `is_suspended` tinyint(1) DEFAULT 0,
  `is_finished` tinyint(1) DEFAULT 0,
  `is_winner` tinyint(1) DEFAULT 0,
  FOREIGN KEY fk_player_user(player_id)
  REFERENCES user(id)
);

-- !Downs
drop table `game`;
drop table `user`;