/*
 Navicat Premium Data Transfer

 Source Server         : local-test
 Source Server Type    : MySQL
 Source Server Version : 50711
 Source Host           : localhost
 Source Database       : weather

 Target Server Type    : MySQL
 Target Server Version : 50711
 File Encoding         : utf-8

 Date: 06/01/2016 17:36:41 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `aqi`
-- ----------------------------
DROP TABLE IF EXISTS `aqi`;
CREATE TABLE `aqi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `PM25` int(11) DEFAULT NULL,
  `O3` int(11) DEFAULT NULL,
  `SO2` int(11) DEFAULT NULL,
  `NO2` int(11) DEFAULT NULL,
  `CO` int(11) DEFAULT NULL,
  `PM10` int(11) DEFAULT NULL,
  `major` int(11) DEFAULT NULL,
  `district` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `aqi_station`
-- ----------------------------
DROP TABLE IF EXISTS `aqi_station`;
CREATE TABLE `aqi_station` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `station` bigint(20) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `PM25` int(11) DEFAULT NULL,
  `PM10` int(11) DEFAULT NULL,
  `O3` int(11) DEFAULT NULL,
  `major` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `city`
-- ----------------------------
DROP TABLE IF EXISTS `city`;
CREATE TABLE `city` (
  `id` bigint(20) NOT NULL,
  `ishot` int(11) NOT NULL,
  `pinyin` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `province` bigint(20) NOT NULL,
  `title` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `district`
-- ----------------------------
DROP TABLE IF EXISTS `district`;
CREATE TABLE `district` (
  `id` bigint(20) NOT NULL,
  `city` bigint(20) NOT NULL,
  `ishot` int(11) NOT NULL,
  `pinyin` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `pinyin_aqi` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `title` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `altitude` decimal(10,5) NOT NULL,
  `area_code` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `latitude` decimal(10,5) NOT NULL,
  `longitude` decimal(10,5) NOT NULL,
  `zipcode` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `province`
-- ----------------------------
DROP TABLE IF EXISTS `province`;
CREATE TABLE `province` (
  `id` bigint(20) NOT NULL,
  `ishot` int(11) NOT NULL,
  `pinyin` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  `title` varchar(30) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `station`
-- ----------------------------
DROP TABLE IF EXISTS `station`;
CREATE TABLE `station` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name_zh` varchar(30) DEFAULT NULL,
  `name_en` varchar(50) DEFAULT NULL,
  `district` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `t_admin`
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `url`
-- ----------------------------
DROP TABLE IF EXISTS `url`;
CREATE TABLE `url` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `weather_forecast`
-- ----------------------------
DROP TABLE IF EXISTS `weather_forecast`;
CREATE TABLE `weather_forecast` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `district` bigint(20) NOT NULL,
  `max` int(11) NOT NULL,
  `min` int(11) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `sunrise` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `sunset` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `wind_direction_day` int(11) DEFAULT NULL,
  `wind_direction_night` int(11) DEFAULT NULL,
  `weather_day` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `weather_night` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `wind_force_day` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  `wind_force_night` varchar(10) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `weather_history`
-- ----------------------------
DROP TABLE IF EXISTS `weather_history`;
CREATE TABLE `weather_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city` bigint(20) NOT NULL,
  `date` date DEFAULT NULL,
  `max` int(11) NOT NULL,
  `min` int(11) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `weather` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `wind_direction` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `wind_force` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `weather_hour`
-- ----------------------------
DROP TABLE IF EXISTS `weather_hour`;
CREATE TABLE `weather_hour` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aqi` int(11) NOT NULL,
  `datetime` datetime DEFAULT NULL,
  `district` bigint(20) NOT NULL,
  `humidity` int(11) NOT NULL,
  `precipitation` decimal(11,3) NOT NULL,
  `temperature` int(11) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `wind_direction` int(11) DEFAULT NULL,
  `wind_force` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
--  Table structure for `weather_instant`
-- ----------------------------
DROP TABLE IF EXISTS `weather_instant`;
CREATE TABLE `weather_instant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `temperature_sensible` decimal(11,3) DEFAULT NULL,
  `datetime` datetime DEFAULT NULL,
  `temperature` decimal(11,3) DEFAULT NULL,
  `precipitation` decimal(11,3) DEFAULT NULL,
  `humidity` int(11) DEFAULT NULL,
  `aqi` int(11) DEFAULT NULL,
  `district` bigint(20) DEFAULT NULL,
  `wind_direction` int(11) DEFAULT NULL,
  `wind_force` int(11) DEFAULT NULL,
  `weather` int(11) DEFAULT NULL,
  `pm25` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
