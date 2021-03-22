package models

import slick.jdbc.PostgresProfile.api._

package object dao {
  implicit def serverConfigToDB(serverConfig: ServerConfig): Database = serverConfig.db
}
