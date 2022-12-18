-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : sam. 17 déc. 2022 à 16:03
-- Version du serveur : 5.7.36
-- Version de PHP : 7.4.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `banque`
--
-- --------------------------------------------------------
DROP DATABASE IF EXISTS banque;
CREATE DATABASE banque;
USE banque;
-- --------------------------------------------------------

--
-- Structure de la table `banquier`
--

DROP TABLE IF EXISTS `banquier`;
CREATE TABLE IF NOT EXISTS `banquier` (
  `idBanquier` varchar(8) NOT NULL,
  `nomBanquier` varchar(26) NOT NULL,
  PRIMARY KEY (`idBanquier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Déchargement des données de la table `banquier`
--

INSERT INTO `banquier` (`idBanquier`, `nomBanquier`) VALUES
('2012clem', 'Clement'),
('2012john', 'John'),
('2012xavi', 'Xavier');

-- --------------------------------------------------------

--
-- Structure de la table `client`
--

DROP TABLE IF EXISTS `client`;
CREATE TABLE IF NOT EXISTS `client` (
  `id` varchar(10) NOT NULL,
  `nom` varchar(26) NOT NULL,
  `password` varchar(26) NOT NULL,
  `solde` int(10) NOT NULL,
  `montantDecouvertAutorise` int(10) NOT NULL,
  `montantPlafond` int(10) NOT NULL,
  `idBanquier` varchar(8) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idBanquier` (`idBanquier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Déchargement des données de la table `client`
--

INSERT INTO `client` (`id`, `nom`, `password`, `solde`, `montantDecouvertAutorise`, `montantPlafond`, `idBanquier`) VALUES
('client1', 'client1', 'client1', 1000, 200, 10000, '2012clem'),
('client2', 'client2', 'client2', 5000, 300, 15000, '2012john'),
('client3', 'client3', 'client3', 10000, 1000, 30000, '2012xavi');

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `client`
--
ALTER TABLE `client`
  ADD CONSTRAINT `client_ibfk_1` FOREIGN KEY (`idBanquier`) REFERENCES `banquier` (`idBanquier`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
