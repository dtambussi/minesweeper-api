# minesweeper-api

Project provides an API to manage minesweeper games

## Availalable endpoints

* Create player user
* List games by player
* Create game associated to player
* Make game associated move (using array indexes, ie: [0,0])
* Suspend game
* Resume game

## Testing the application (use Content-Type: application/json for requests)

Application is deployed to a DigitalOcean server at 138.197.111.116 (port 9000). 

Sample invocations:

**Create a user**

POST http://138.197.111.116:9000/user

```javascript
{"nickname": "dt"}
```
Reponse:

```javascript
{
    "id": 1,
    "nickname": "dt"
}
```
**Add a game with created user as player**

POST 138.197.111.116:9000/game

```javascript
{
	"userId": 1,
	"rowCount": 4,
	"columnCount": 4,
	"mineCount": 2
}
```
Response:

```javascript
{
    "id": 1,
    "rowCount": 4,
    "columnCount": 4,
    "mineCount": 2,
    "totalTimeInSecs": 0,
    "isSuspended": false,
    "isFinished": false,
    "isWinner": false,
    "createdAt": "2019-04-22T17:44:30.000+0000",
    "latestInteractionAt": "2019-04-22T17:44:30.000+0000",
    "board": [
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ]
    ]
}
```
**User makes a move to reveal first item from first row**

POST 138.197.111.116:9000/game/1/moveRequest

```javascript
{
	"userId": 1,
	"moveType": "Reveal",
	"rowIndex": 0,
	"colIndex": 0
}
```
Response:

```javascript
{
    "id": 1,
    "rowCount": 4,
    "columnCount": 4,
    "mineCount": 2,
    "totalTimeInSecs": 1846,
    "isSuspended": false,
    "isFinished": false,
    "isWinner": false,
    "createdAt": "2019-04-22T17:44:30.000+0000",
    "latestInteractionAt": "2019-04-22T18:15:16.000+0000",
    "board": [
        [
            "1",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ]
    ]
}
```
A Hint was revealed, game continues

**User makes a move to reveal another cell but hits a mine**

POST 138.197.111.116:9000/game/1/moveRequest (possible moveType values are: Reveal, FlagAsMine, QuestionMark)

```javascript
{
	"userId": 1,
	"moveType": "Reveal",
	"rowIndex": 1,
	"colIndex": 2
}
```
Response:

```javascript
{
    "id": 1,
    "rowCount": 4,
    "columnCount": 4,
    "mineCount": 2,
    "totalTimeInSecs": 2231,
    "isSuspended": false,
    "isFinished": true,
    "isWinner": false,
    "createdAt": "2019-04-22T17:44:30.000+0000",
    "latestInteractionAt": "2019-04-22T18:21:41.000+0000",
    "board": [
        [
            "1",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "*",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ],
        [
            "-",
            "-",
            "-",
            "-"
        ]
    ]
}
```
Game is finished

**Suspend game example (a suspendable one)**

POST 138.197.111.116:9000/game/2/suspensionRequest

```javascript
{
	"userId": 1
}
```
Response:

```javascript
{
    "id": 2,
    "rowCount": 8,
    "columnCount": 8,
    "mineCount": 15,
    "totalTimeInSecs": 66,
    "isSuspended": true,
    "isFinished": false,
    "isWinner": false,
    "createdAt": "2019-04-22T18:26:35.000+0000",
    "latestInteractionAt": "2019-04-22T18:27:42.000+0000",
    "board": "..." // board contents
}
```

**Resume game example (a suspended one)**

POST 138.197.111.116:9000/game/2/resumeRequest

```javascript
{
	"userId": 1
}
```

Response:

```javascript
{
    "id": 2,
    "rowCount": 8,
    "columnCount": 8,
    "mineCount": 15,
    "totalTimeInSecs": 66,
    "isSuspended": false,
    "isFinished": false,
    "isWinner": false,
    "createdAt": "2019-04-22T18:26:35.000+0000",
    "latestInteractionAt": "2019-04-22T18:27:42.000+0000",
    "board": "..." // board contents
}
```

**List games (just returns an array with the established game json format)**

GET http://138.197.111.116:9000/users/1/games

## About implementation

* User model is not enforced all the way to having sessions, tried not to spend much time with it
* No tests present, however I did manual test using POSTMAN
* Probably a json oriented NoSQL DB would have been a better choice, or even PostgreSQL,
  but tried to make it work with my most usual setup to focus on model implementation 
* Client implementation in a different language will be added later
* Commented on Board model toString method so it is understood how board contents are stored in DB
* There is room for exploring border scenarios, I tried with less that 1000 rows/cols. For instance,
  if mine count is more than half the cells we could switch to an implementation that uses mines as
  default cell value and randomly places empty cells.
