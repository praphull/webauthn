run:
	@sbt run -Dhttps.port=9443 -Dhttp.port=disabled -Dconfig.file=/opt/play-confs/webauthn.conf
run-http:
	@sbt run -Dconfig.file=/opt/play-confs/webauthn.conf
