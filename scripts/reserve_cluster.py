import requests, json

data = json.loads(requests.get("http://localhost:5050/slaves").content)
agents = [item.get('id') for item in data.get("slaves")]

for agent in agents[0:2]:

	r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agent, 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant1"               }             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant1"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant1"               }             ]           }         ]'})

	print r.status_code

for agent in agents[2:4]:

	r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agent, 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant2"               }             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant2"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant2"               }             ]           }         ]'})

	print r.status_code

# TENANT 1 private 

r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agents[0], 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant1"               },{                 "type": "DYNAMIC",                  "role": "tenant1/private"}             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant1"},{                 "type": "DYNAMIC",                  "role": "tenant1/private"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant1"               },{                 "type": "DYNAMIC",                  "role": "tenant1/private"}             ]           }         ]'})

print r.status_code

# TENANT 1 public

r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agents[1], 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant1"               },{                 "type": "DYNAMIC",                  "role": "tenant1/public"}             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant1"},{                 "type": "DYNAMIC",                  "role": "tenant1/public"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant1"               },{                 "type": "DYNAMIC",                  "role": "tenant1/public"}             ]           }         ]'})

print r.status_code


# TENANT 2 private

r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agents[2], 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant2"               },{                 "type": "DYNAMIC",                  "role": "tenant2/private"}             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant2"},{                 "type": "DYNAMIC",                  "role": "tenant2/private"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant2"               },{                 "type": "DYNAMIC",                  "role": "tenant2/private"}             ]           }         ]'})

print r.status_code


# TENANT 2 public

r = requests.post("http://localhost:5050/master/reserve", data={'slaveId': agents[3], 'resources': '[           {             "name": "cpus",             "type": "SCALAR" ,             "scalar": { "value": 1 },             "reservations": [               {                 "type": "DYNAMIC",                 "role": "tenant2"               },{                 "type": "DYNAMIC",                  "role": "tenant2/public"}             ]           },            {             "name": "mem",             "type": "SCALAR",             "scalar": { "value": 1024 },             "reservations": [               {                 "type": "DYNAMIC",                  "role": "tenant2"},{                 "type": "DYNAMIC",                  "role": "tenant2/public"}             ]           },   {             "name": "disk",             "type": "SCALAR",             "scalar": { "value": 10000 },             "reservations": [               {                  "type": "DYNAMIC",                 "role": "tenant2"               },{                 "type": "DYNAMIC",                  "role": "tenant2/public"}             ]           }         ]'})

print r.status_code
