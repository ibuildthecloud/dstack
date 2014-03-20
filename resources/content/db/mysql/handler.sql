
DROP TABLE IF EXISTS `external_handler`;
CREATE TABLE `external_handler` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `kind` varchar(255) NOT NULL,
  `uuid` varchar(128) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `state` varchar(128) NOT NULL,
  `created` datetime DEFAULT NULL,
  `removed` datetime DEFAULT NULL,
  `remove_time` datetime DEFAULT NULL,
  `data` text,
  `priority` int(10) DEFAULT NULL,
  KEY `idx_external_handler_name` (`name`),
  KEY `idx_external_handler_removed` (`removed`),
  UNIQUE KEY `idx_external_handler_uuid` (`uuid`),
  KEY `idx_external_handler_remove_time` (`remove_time`),
  KEY `idx_external_handler_state` (`state`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `external_handler_process`;
CREATE TABLE `external_handler_process` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `kind` varchar(255) NOT NULL,
  `uuid` varchar(128) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `state` varchar(128) NOT NULL,
  `created` datetime DEFAULT NULL,
  `removed` datetime DEFAULT NULL,
  `remove_time` datetime DEFAULT NULL,
  `data` text,
  KEY `idx_external_handler_process_name` (`name`),
  KEY `idx_external_handler_process_removed` (`removed`),
  UNIQUE KEY `idx_external_handler_process_uuid` (`uuid`),
  KEY `idx_external_handler_process_remove_time` (`remove_time`),
  KEY `idx_external_handler_process_state` (`state`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `external_handler_external_handler_process_map`;
CREATE TABLE `external_handler_external_handler_process_map` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `kind` varchar(255) NOT NULL,
  `uuid` varchar(128) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `state` varchar(128) NOT NULL,
  `created` datetime DEFAULT NULL,
  `removed` datetime DEFAULT NULL,
  `remove_time` datetime DEFAULT NULL,
  `data` text,
  `external_handler_id` bigint(19),
  `external_handler_process_id` bigint(19),
  KEY `idx_eh_eh_process_map_name` (`name`),
  KEY `idx_eh_eh_process_map_removed` (`removed`),
  UNIQUE KEY `idx_eh_eh_process_map_uuid` (`uuid`),
  KEY `idx_eh_eh_process_map_remove_time` (`remove_time`),
  KEY `idx_eh_eh_process_map_state` (`state`),
  KEY `fk_eh_eh_process_map_external_handler_id` (`external_handler_id`),
  CONSTRAINT `fk_eh_eh_process_map__external_handler_id` FOREIGN KEY (`external_handler_id`) REFERENCES `external_handler` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  KEY `fk_eh_eh_process_map_external_handler_process_id` (`external_handler_process_id`),
  CONSTRAINT `fk_eh_eh_process_map__external_handler_process_id` FOREIGN KEY (`external_handler_process_id`) REFERENCES `external_handler_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
