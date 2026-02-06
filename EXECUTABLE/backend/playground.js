use('basyx_dpp')
db.createCollection('dpps')

use('admin')
db.createUser({
  user: "dppUser",
  pwd: "dppPass", 
  roles: [{ role: "readWrite", db: "basyx_dpp" }]
})

use('basyx_dpp')
db.auth("dppUser", "dppPass")