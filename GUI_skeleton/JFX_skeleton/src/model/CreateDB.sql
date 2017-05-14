DROP DATABASE IF EXISTS RAS;
CREATE DATABASE RAS;

USE RAS;


CREATE TABLE Orders (
	OrderID int not null,
	ServerID int not null,
	TableID int not null,
	OrderStatus int not null,
	OrderTotal double not null,
	PRIMARY KEY (OrderID)
);



CREATE TABLE menuitem(
	item_id INT NOT NULL PRIMARY KEY,
	item_name VARCHAR(30) NOT NULL,
	item_price DOUBLE NOT NULL,
	item_description VARCHAR(200) NOT NULL);

CREATE TABLE Order_Items (
	OrderID int not null,
	Seat_Number int not null DEFAULT '0',
	Menu_Item_Id int not null,
	item_name VARCHAR(30) NOT NULL,
	item_price FLOAT NOT NULL,
	item_description VARCHAR(200) NOT NULL,
	PRIMARY KEY(OrderID,Menu_Item_Id,Seat_Number),
	FOREIGN KEY (OrderID) REFERENCES Orders (OrderID)

);

-- FOREIGN KEY (Menu_Item_Id) REFERENCES menuitem (item_id)

--
-- Table structure for table `Employees`
--

CREATE TABLE `Employees` (
  `employeeID` int(3) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Employee_name` varchar(75) NOT NULL,
  `Employee_type` varchar(25) NOT NULL,
  `working_now` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- --------------------------------------------------------

--
-- Table structure for table `Tables`
--

CREATE TABLE `Tables` (
  `tableID` int(2) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `employeeID` int(3) NOT NULL,
  `max_occupancy` int(2) NOT NULL,
  `Table_status` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `reservation` (
  `resv_id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(20) DEFAULT NULL,
  `last_name` varchar(20) DEFAULT NULL,
  `guest_count` int(11) DEFAULT NULL,
  `res_date` varchar(20) DEFAULT NULL,
  `res_time` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`resv_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

CREATE TABLE `wait_list` (
  `guest_id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(20) DEFAULT NULL,
  `last_name` varchar(20) DEFAULT NULL,
  `guest_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

 