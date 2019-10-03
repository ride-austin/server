FROM mysql:5
ADD my.cnf /etc/mysql/my.cnf
CMD ["mysqld", "--datadir=/var/lib/mysql", "--user=mysql", "--disable-partition-engine-check"]