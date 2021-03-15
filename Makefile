run:
	@sbt run -Dhttps.port=9443 -Dhttp.port=disabled
run-http:
	@sbt run
