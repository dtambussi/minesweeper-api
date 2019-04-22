import datetime
import requests

# Tested this script by running it from https://repl.it/languages/python

BASE_URL = "http://138.197.111.116:9000"
DEFAULT_HEADERS = {'Content-Type': 'application/json'}
CREATE_USER_URL = BASE_URL + "/user"
CREATE_GAME_URL = BASE_URL + "/game"

def randomUserCreationData():
  randomNumber = datetime.datetime.now().strftime('%Y/%m/%d %H:%M:%S.%f')[:-3]
  return '{"nickname":"%s"}' % ("user" + str(randomNumber))

def gameCreationData(userId, rows, cols, mines):
  return '{"userId":"%s", "rowCount":"%s", "columnCount":"%s", "mineCount":"%s"}' % (str(userId), str(rows), str(cols), str(mines))

def makeMoveData(userId, moveType, row, col):
  return '{"userId":"%s", "moveType":"%s", "rowIndex":"%s", "colIndex":"%s"}' % (str(userId), moveType, row, col)

def listGamesUrl(playerUserId):
  return BASE_URL + "/users/" + str(playerUserId) + '/games'

def makeMoveUrl(gameId):
  return BASE_URL + "/game/" + str(gameId) + '/moveRequest'

# Create user
createUserResponse = requests.post(CREATE_USER_URL, headers = DEFAULT_HEADERS, data = randomUserCreationData())
if createUserResponse.status_code != 200:
  raise Exception('user.create.error ' + createUserResponse.text)

print("Create user response: " + createUserResponse.text + "\n")
userId = createUserResponse.json()["id"]

# Create game
newGameData = gameCreationData(userId, 4, 4, 2)
createGameResponse = requests.post(CREATE_GAME_URL, headers = DEFAULT_HEADERS, data = newGameData)
if createGameResponse.status_code != 200:
  raise Exception('game.create.error ' + createGameResponse.text)

print("Create game response: " + createGameResponse.text + "\n")
gameId = createGameResponse.json()["id"]

# Make move
makeMoveData = makeMoveData(userId, "Reveal", 0, 0)
makeMoveResponse = requests.post(makeMoveUrl(gameId), headers = DEFAULT_HEADERS, data = makeMoveData)
if makeMoveResponse.status_code != 200:
  raise Exception('game.move.error ' + makeMoveResponse.text)

print("Make move response: " + makeMoveResponse.text + "\n")

# List games
listUserGamesResponse = requests.get(listGamesUrl(userId))
print("List games response for user with id: " + str(userId) + "\n" + makeMoveResponse.text + "\n")

print("Client tests successful")
