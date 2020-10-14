//UI
//TODO: Button with text either "join current game" or "start game"
//TODO: Nice name input (if starting game: ask if you want computer to play, level, and amount of AI's)
//TODO: nice guess input
//TODO: nice timer
//TODO: nice player current guess and position (different for each player)
//TODO: top guess and (guess list??)
//TODO: Player List (name, game score, current guess)
//TODO: put back button
//TODO: add trail of movement?
//TODO: highlight clicked robot, robot stays "clicked"
//TODO: make "please put proper input" nice, not an alert
//TODO: because of reset game, make submitting player name look nicer

//BUGS
//TODO: change way to catch up when load afterwards
//TODO: have game be deleted after certain time of inactivity
//TODO: when someone joins a game and leaves without putting in a username, the game is not deleted
//TODO: remove player when tab is closed
//TODO: cant submit guess if dont have name

//FLOW OF PROGRAM
//TODO: Fix display join game buttons

//Features
//TODO: make boards randomized at start of game
//TODO: add back button

// Constants
CELL_WIDTH = 40;
CELL_HEIGHT = 40;
WALL_THICKNESS = 3;

// Game State
gameIdsForLobby = null;
gameId = null;
mostRecentlyClickedRobotColor = null;
canvasBoard = null;
contextBoard = null;
canvasLine = null;
contextLine = null;
playerMoves = 0;
currentVersion = -1;
versionOnLoad = 0; // used to allow new players to automatically catch up to
                    // the version on load
playerId = "test";
robotInMotion = false; // used so we do not double count moves (when a user clicks twice in the same direction and it counts
// that as two moves as opposed to one). Will not count moves when this variable is true.
justRestartedGame = false; //allows restart game to call setup method without updating to current gameState



// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
/*googleAppEngineChannel = null;

function createGoogleAppEngineChannel(userId) {
  // ADD CODE TO get unique userId token from server
  // var token =
  googleAppEngineChannel = new goog.appengine.Channel(
);
  // TODO: Will 'onGoogleAppEngineChannelOpened' work like this? Or do I need to
  // assign it to a var?
  var handler = {
    'onopen': onGoogleAppEngineChannelOpened,
    'onmessage': onGoogleAppEngineMessageReceived,
    'onerror': function() {},
    'onclose': function() {}
  };
  var socket = googleAppEngineChannel.open(handler);
  socket.onopen = onGoogleAppEngineChannelOpened;
  socket.onmessage = onMessage;
}

function sendGoogleAppEngineMessage(path, opt_param) {
  // Change this to build query
  path += '?g=' + state.game_key;
  if (opt_param) {
    path += '&' + opt_param;
  }
  
  // Change this to jquery ajax
  var xhr = new XMLHttpRequest();
  xhr.open('POST', path, true);
  xhr.send();
}

function onGoogleAppEngineChannelOpened() {
  // Change this to send appropriate message
  sendGoogleAppEngineMessage('/opened');
}

function onGoogleAppEngineMessageReceived() {
  // Update client here
}
// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
// WORK IN PROGRESS -- see https://cloud.google.com/appengine/docs/java/channel/
*/

function init() {
  ajaxGetGameIds();
  document.getElementById("joinGameButtons").hidden = false;
  document.getElementById("heading").hidden = false;
  document.getElementById("wrapper").style.visibility = 'hidden';
  document.getElementById("inputs").style.visibility = 'hidden';
  document.getElementById("debugDiv").style.visibility = 'hidden';
 }

function startGame(gameIdAssigned) {
  gameId = gameIdAssigned;
  document.getElementById("h1").innerHTML = "Ricochet Robots " + gameId;
  document.getElementById("wrapper").style.visibility = 'visible';
  document.getElementById("inputs").style.visibility = 'visible';
  document.getElementById("debugDiv").style.visibility = 'hidden';
  document.getElementById("startButton").style.visibility = 'hidden';
  document.getElementById("joinGameButtons").hidden = true;
    
  canvasBoard = document.getElementById("canvasBoard");
  contextBoard = canvasBoard.getContext("2d");
  canvasLine = document.getElementById("canvasLine");
  contextLine = canvasLine.getContext("2d");
  canvasTime = document.getElementById("canvasTime");
  contextTime = canvasTime.getContext("2d");
  canvasTime.style.top = cellYToY(7);
  canvasTime.style.left = cellXToX(7);
  canvasTime.style.width = CELL_WIDTH * 2;
  canvasTime.style.height = CELL_HEIGHT * 2;

  
  
  var cells = 16;
  for (var x = 0; x < (CELL_WIDTH * cells); x = x + CELL_WIDTH) {
    for (var y = 0; y < (CELL_HEIGHT * cells); y = y + CELL_HEIGHT) {
      contextBoard.strokeStyle = "grey";
      contextBoard.lineWidth = 1;
      contextBoard.strokeRect(x, y, CELL_WIDTH, CELL_HEIGHT);
    }
  }

  // fill center with black
  contextBoard.fillRect(cellXToX(7), cellYToY(7), CELL_WIDTH * 2,
      CELL_HEIGHT * 2);
  
  ajaxBuildWalls();
  ajaxBuildTargets();
  buildRobots();
  
  canvasBoard.addEventListener("mousedown", ajaxMoveRobotTowardCanvasClick, false);
  window.addEventListener("keydown", ajaxMoveRobotInKeyDirection, false);
  
  ajaxGetCurrentGameVersion();
  /*
   * ajaxGetCurrentGameState(); setTimeout(function()
   * {ajaxGetLatestChangesFromServer();}, 100);
    Set a  timeout  so the  current  version can be updated before we request the changes. 
    Otherwise we would get all the changes from version
   */
                        
  if(justRestartedGame){
    justRestartedGame = false;
    //clear none board items
    addGuesses({});
    updatePlayersAndScoresList({});
    showPlayerToMove("");
    document.getElementById("playerName").innerHTML = "";
    document.getElementById('nameBox').hidden = false;
    document.getElementById('submitName').hidden = false;
    document.getElementById('submitName').innerHTML = 'Add Player';
  } else {
    ajaxGetLatestChangesFromServer();
  }
  
  getRobotFromColor("Red").style.visibility = "visible";
  getRobotFromColor("Yellow").style.visibility = "visible";
  getRobotFromColor("Green").style.visibility = "visible";
  getRobotFromColor("Blue").style.visibility = "visible";
}

function ajaxGetCurrentGameState(){
  // Get the board state
  // Get the players and scoress
  $.ajax({
    url : "/ricochet/getcurrentgamestate?gameId=" + gameId,
    success : function(result){
      var gameState = JSON.parse(result);
      console.log(result);
      console.log(gameState);
      updateGameState(gameState);
    }
  });
}

function ajaxGetCurrentGameVersion(){
  // Get the board state
  // Get the current version
  // Get the players and scoress
  $.ajax({
    url : "/ricochet/getcurrrentversion?gameId=" + gameId,
    success : function(result){
      var version = JSON.parse(result);
      versionOnLoad = version.version;
    }
  });
}

function ajaxGetLatestChangesFromServer() {
  displayLatestChanges("-------------------------");
  displayLatestChanges("Requesting changes since version " + currentVersion);
  $.ajax({
    url : "/ricochet/getlatestchanges?gameId=" + gameId + "&version=" + currentVersion,
    success : function(result) {
      var updateEventList = JSON.parse(result);
      var eventCount = updateEventList.length;
      var msg = "Received changes since version " + currentVersion + ". ";
      if (eventCount == 0) {
        msg = msg + "No events.";
      } else {
        document.getElementById("latestChangesMessages").style.backgroundColor = "yellow";
        var minEventVersion = updateEventList[0].currentVersion;
        var maxEventVersion = updateEventList[eventCount - 1].currentVersion;
        document.getElementById("highestKnownServerVersion").innerHTML = maxEventVersion; 
        msg = msg + eventCount + " events (" + minEventVersion + "-" + maxEventVersion + "): " + result;
      }
      displayLatestChanges(msg);

      // Create a function that processes a single element in the list, and when
      // done
      // calls itself with the next element. If there are no elements in list,
      // then
      // request more changes from server. Need to do it this way because some
      // events
      // use setTimeout and therefore we can't just process all events in simple
      // loop.
      var processUpdateEventList = function(updateEventList, index) {
        if (index < updateEventList.length) {
          // We have an event to process. Process it and have that call back to
          // us when
          // done with the next index position.
          processUpdateEvent(
              updateEventList[index],
              function() {
                processUpdateEventList(updateEventList, index + 1);
              });
        } else {
          // No more events to process -- get more from server.
          document.getElementById("latestChangesMessages").style.backgroundColor = null;
          setTimeout(ajaxGetLatestChangesFromServer, 10);          
        } 
      };
      
      // Start processing the list by calling function with index of 0.
      processUpdateEventList(updateEventList, 0);
    }
  });
}

function processUpdateEvent(updateEvent, actionWhenDone) {
  var shouldExecuteActionWhenDone = true;
  
  switch (updateEvent.eventType){
    case "GAME_RESTARTED":
      writeMessage("Game restarted on server.");
      clearLatestChangesMessages();
      justRestartedGame = true;
      
      var newGameId = updateEvent.eventData.newGameId;
      //restart game
      startGame(newGameId);
      break;
    case "TIMER_CHANGED":
      displayTime(updateEvent.eventData.timerValue);
      break;
    case "PLAYER_TO_MOVE_CHANGED":
      showPlayerToMove("Player to Move:" + updateEvent.eventData.player);
      writeMessage("");
      break;
    case "TARGET_SET":
      var oldTarget = updateEvent.eventData.oldTarget;
      var newTarget = updateEvent.eventData.newTarget;
      var position = updateEvent.eventData.position;
      clearAllTargetsAndShowNewTarget(newTarget, position);
      break;
    case "ROBOT_GLIDED":
      var robot = getRobotFromColor(updateEvent.eventData.robot);
      var oldPositon = updateEvent.eventData.oldPosition;
      var newPosition = updateEvent.eventData.newPosition;
      var direction = updateEvent.eventData.direction;
      var playerMoves = updateEvent.eventData.numberOfMoves;
      writeMessage("Moves: " + playerMoves);
      // Purpose: When a new page is loaded (new player joins), the robots jump
      // straight to their current game state instead of
      // gliding through every previous move.
      if (currentVersion <= versionOnLoad){
        moveRobotJumpToPosition(robot, newPosition, actionWhenDone);
      } else {
        moveRobotGlideToPosition(robot, direction, newPosition, actionWhenDone);
        shouldExecuteActionWhenDone = false;
      }
      break;
    case "ROBOT_JUMPED":
      var robot = getRobotFromColor(updateEvent.eventData.robot);
      var oldPositon = updateEvent.eventData.oldPosition;
      var newPosition = updateEvent.eventData.newPosition;
      moveRobotJumpToPosition(robot, newPosition, actionWhenDone);
      break;
    case "PLAYER_LIST_CHANGED":
      var playersAndScores = updateEvent.eventData.playersAndScores;
      updatePlayersAndScoresList(playersAndScores);
      break;
    case "GUESS_SUBMITTED":
      addGuesses(updateEvent.eventData.playersAndGuesses);  
      break;
    case "SCORE_UPDATED":
      var playersAndScores = updateEvent.eventData.playersAndScores;
      updatePlayersAndScoresList(playersAndScores);
  }
  
  if (shouldExecuteActionWhenDone) {
    actionWhenDone();
  }
  currentVersion = updateEvent.currentVersion;
  document.getElementById("currentClientVersion").innerHTML = currentVersion; 
}

function ajaxStartGame(){
  $.ajax({
    url : "/ricochet/game/start",
    success : function(result) {
      var gameId = JSON.parse(result).gameId;
      startGame(gameId);
    }  
  });
}

function ajaxGetGameIds(){
  $.ajax({
    url : "/ricochet/gameids",
    success : function(result) {
      var gameIds = JSON.parse(result);
      console.log(gameIds.length);
      gameIdsForLobby = gameIds;
      createJoinGameButtons();
    }  
  });
}

function ajaxRestartGame(){  
  $.ajax({
    url : "/ricochet/game/restart?gameId=" + gameId,
    success : function(result) {
    }
  });
}

function ajaxSubmitGuess(){
  var guess = document.getElementById('guessBox').value;
  console.log(guess);
  if (!Number.isInteger(Number(guess))){ 
    // If the inputed guess is not an Integer, do not submit the guess to the
    // server and prompt the user to re-guess
    alert("Invalid Guess. Please Submit a New Guess");
    return;
  }
  $.ajax({
    url : "/ricochet/submit/guess?gameId=" + gameId + "&guesserId=" + playerId + "&guess=" + guess,
    success : function(result) {
    }
  });
}

function ajaxAddNewPlayer(){
  playerId = document.getElementById('nameBox').value;
  $.ajax({
    url : "/ricochet/submit/newplayer?gameId=" + gameId + "&playerId=" + playerId,
    success : function(result) {
      var json = JSON.parse(result);
      if(JSON.parse(result).isValid == "true"){
        var parent = document.getElementById("playerInputs");
//      parent.removeChild(document.getElementById('nameBox'));
//      parent.removeChild(document.getElementById('submitName'));
        document.getElementById('nameBox').hidden = true;
        document.getElementById('submitName').hidden = true;
        document.getElementById('submitName').innerHTML = '';
        document.getElementById("playerName").innerHTML = playerId;
      }
    }
  });
}

function ajaxLeaveGame() {
  $.ajax({
    url : "/ricochet/submit/removeplayer?gameId=" + gameId + "&playerId=" + playerId,
    success : function(result) {
      location.reload();
      }
    });
}


function ajaxBuildWalls() {
  $.ajax({
    url : "/ricochet/board/walls/get?gameId=" + gameId,
    success : function(result) {
      var boardItems = JSON.parse(result);
      buildWalls(boardItems);
    }
  });
} 

function ajaxBuildTargets(){
  $.ajax({
    url : "/ricochet/board/targets/get?gameId=" + gameId,
    success : function(result) {
      var targetsAndPositions = JSON.parse(result);
      console.log(targetsAndPositions.targets + ", " + targetsAndPositions.positions);
      buildTargetsListBox(targetsAndPositions.targets, targetsAndPositions.positions);
    }
  });
}

function ajaxGetBoardState() {
  $.ajax({
    url : "/ricochet/boardstate/get?gameId=" + gameId,
    success : function(result) {
      var boardState = JSON.parse(result);
      var target = boardState.chosenTarget;
      var robotPositions = boardState.robotPositions;
      writeMessage(
          "Target: " + target.color + " " + target.shape
              + "   Red: " + robotPositions.positions[0].x + "," + robotPositions.positions[0].y
              + "   Yellow: " + robotPositions.positions[1].x + "," + robotPositions.positions[1].y
              + "   Green: " + robotPositions.positions[2].x + "," + robotPositions.positions[2].y
              + "   Blue: " + robotPositions.positions[3].x + "," + robotPositions.positions[3].y
              );
    }
  });
}

function ajaxSetTarget(targetColorAndShapeString) {
  var array = targetColorAndShapeString.split(" ");
  $.ajax({
    url : "/ricochet/game/target/set?gameId=" + gameId + "&color=" + array[0] + "&shape=" + array[1],
    success : function(result) {
    }
  });
}

function ajaxSolveGame(numberOfRounds) {
  writeMessage("Solving " + numberOfRounds + " rounds. Please have patience...");
  $.ajax({
    url : "/ricochet/game/solve?gameId=" + gameId + "&numberOfRounds=" + numberOfRounds,
    success : function(result) {
    }
  });
}

function ajaxMoveRobotInKeyDirection(event){
  console.log("Key Code " + event.keyCode);
  var robotColor = mostRecentlyClickedRobotColor;
  if(event.keyCode === 38){
    ajaxMoveRobotTowardDirection(robotColor, "North");
    console.log("North");
  } else if(event.keyCode === 39){
    ajaxMoveRobotTowardDirection(robotColor, "East");
    console.log("East")
  } else if(event.keyCode === 40){
    console.log("South")
    ajaxMoveRobotTowardDirection(robotColor, "South");
  } else if(event.keyCode === 37){
    console.log("West")
    ajaxMoveRobotTowardDirection(robotColor, "West");
  }
}

function ajaxMoveRobotTowardCanvasClick(event) {
  var pos = getPositionOnCanvas(canvasBoard, event);
  ajaxMoveRobotTowardPosition(mostRecentlyClickedRobotColor, pos);
}

function ajaxMoveRobotTowardPosition(robotColor, pos) {
  var direction = getDirectionOfPositionRelativeToRobot(pos, getRobotFromColor(robotColor));
  if (direction != null){
    ajaxMoveRobotTowardDirection(robotColor, direction);
  }
}

function ajaxMoveRobotTowardDirection(robotColor, direction, actionWhenDone) {
  console.log(robotInMotion);
  if (robotInMotion){
    return; 
    // If a robot is currently in motion, we will wait till it's done moving to
    // accept more moves in order to not double count
  }
  robotInMotion = true;
  $.ajax({
    url : "/ricochet/robot/move?gameId=" + gameId + "&robot=" + robotColor + "&direction="
        + direction + "&moverId=" + playerId,
    success : function(result) {
    }
  });
}

function ajaxCheckForWinner(robot) {
  $.ajax({
    url : "/ricochet/robot/iswinner?gameId=" + gameId + "&robot=" + getColorFromRobot(robot),
    success : function(result) {
      var isWinner = JSON.parse(result);
      if (isWinner == true) {
        writeMessage("CONGRATULATIONS! YOU WON: " + result);
        ajaxChooseNewTarget();
      }
    }
  });
}


function targetSelectedFromListBox() {
  var targetListBox = document.getElementById("targetListBox");
  var selectedText = targetListBox.options[targetListBox.selectedIndex].text;
  ajaxSetTarget(selectedText);
}

function ajaxChooseNewTarget() {
  $.ajax({
    url : "/ricochet/game/target/chooseNew?gameId=" + gameId,
    success : function(result) {
    }
  });
}

function buildWalls(boardItems) {
  for (var i = 0; i < boardItems.length; i++) {
    var boardItem = boardItems[i];
    if (boardItem.northWall === true) {
      buildWall(boardItem.position.x, boardItem.position.y, "NORTH");
    }
    if (boardItem.eastWall === true) {
      buildWall(boardItem.position.x, boardItem.position.y, "EAST");
    }
    if (boardItem.southWall === true) {
      buildWall(boardItem.position.x, boardItem.position.y, "SOUTH");
    }
    if (boardItem.westWall === true) {
      buildWall(boardItem.position.x, boardItem.position.y, "WEST");
    }
  }
}

function buildWall(cellX, cellY, direction) {
  var x = cellXToX(cellX) - 1;
  var y = cellYToY(cellY) - 1;
  contextBoard.strokeStyle = "black";
  switch (direction) {
  case "NORTH":
    contextBoard.fillRect(x, y, CELL_WIDTH, WALL_THICKNESS);
    break;
  case "EAST":
    contextBoard.fillRect(x + CELL_WIDTH, y, WALL_THICKNESS, CELL_HEIGHT + 3);
    break;
  case "SOUTH":
    contextBoard.fillRect(x, y + CELL_HEIGHT, CELL_WIDTH + 3, WALL_THICKNESS);
    break;
  case "WEST":
    contextBoard.fillRect(x, y, WALL_THICKNESS, CELL_HEIGHT);
    break;
  default:
    writeMessage("Unknown direction: " + direction);
  }
}

function buildTargetsListBox(targets, positions){
  var targetListBox = document.getElementById("targetListBox");
  for (var i=0; i<targets.length; i++){
    var target = targets[i];
    var targetOption = document.createElement('option');
    targetOption.text = target.color + " " + target.shape;
    targetListBox.add(targetOption); 
  }
}

function clearAllTargetsAndShowNewTarget(target, position) {
  clearAllTargets();
  buildTarget(target, position);
}

function buildTarget(target, position) {
  // contextBoard.fillRect(cellXToX(position.x), cellYToY(position.y),
  // CELL_HEIGHT, CELL_WIDTH);
  var GAP = 8;
  var svg   = document.getElementById("boardSvg");
  var svgNS = svg.namespaceURI;
  
  var svgShape = 'rect';
  switch (target.shape) {
    case 'Star':
      svgShape = 'rect';
      break;
    case 'Planet':
      svgShape = 'rect';
      break;
    case 'Moon':
      svgShape = 'rect';
      break;
    case 'Sawblade':
      svgShape = 'rect';
      break;
  }
  
  var rect = document.createElementNS(svgNS,svgShape);
  rect.setAttribute('id', target.color.charAt(0) + "_" + target.shape.substring(0,2));
  rect.setAttribute('class', "target");
  rect.setAttribute('x',cellXToX(position.x) + GAP);
  rect.setAttribute('y',cellYToY(position.y) + GAP);
  rect.setAttribute('width',CELL_WIDTH - (GAP * 2));
  rect.setAttribute('height',CELL_HEIGHT - (GAP * 2));
  rect.setAttribute('fill',target.color);
  rect.setAttribute('color', target.color);
  rect.setAttribute('shape', target.shape);
// rect.style.visibility = 'hidden';
  svg.appendChild(rect);
}

function buildRobots(){
  var GAP = 8;
  var svg   = document.getElementById("boardSvg");
  var svgNS = svg.namespaceURI;
  
  var redRobot = document.createElementNS(svgNS, 'circle');
  redRobot.setAttribute('id', "redRobot");
  
}

function clearAllTargets(){
  var svg = document.getElementById("boardSvg");
  var targets = document.getElementsByClassName("target");
  for (var i = 0; i < targets.length; i++) {
    svg.removeChild(targets[i]);    
  }
}


function moveRobotJumpToPosition(robot, cellPosition) {
  robot.style.left = cellXToX(cellPosition.x);
  robot.style.top = cellYToY(cellPosition.y);
}

function moveRobotGlideToPosition(robot, direction, cellPosition, actionWhenDone) {
// contextLine.beginPath();
  switch (direction) {
  case 'North':
  case 'South':
    moveVerticalGlide(robot, cellYToY(cellPosition.y), actionWhenDone);
    break;
  case 'East':
  case 'West':
    moveHorizontalGlide(robot, cellXToX(cellPosition.x), actionWhenDone);
    break;
  }
}

function moveVerticalGlide(robot, newY, actionWhenDone) {
  var y = robot.offsetTop;
  var x = robot.offsetLeft;
//  contextLine.moveTo(x, y);
  if (newY > y) {
    robot.style.top = y + 2;
//    contextLine.lineTo(x, y + 5)
  }
  if (newY < y) {
    robot.style.top = y - 2;
//    contextLine.lineTo(x, y - 5)
  }
  contextLine.stroke();
  if (newY != y) {
    setTimeout(function() {
      moveVerticalGlide(robot, newY, actionWhenDone)
    }, 1);
  } else {
    robotInMotion = false; // at this point, a robot has finished its motion.
                            // We can now look for other moves.
    ajaxCheckForWinner(robot);
    if (actionWhenDone != null) {
      actionWhenDone();
    }
  }
}

function moveHorizontalGlide(robot, newX, actionWhenDone) {
  var x = robot.offsetLeft;
  if (newX > x) {
   robot.style.left = x + 2;
  }
  if (newX < x) {
    robot.style.left = x - 2;
  }
  if (newX != x) {
    setTimeout(function() {
      moveHorizontalGlide(robot, newX, actionWhenDone)
    }, 1);
  } else {
    ajaxCheckForWinner(robot);
    robotInMotion = false; // at this point, a robot has finished its motion.
                            // We can now look for other moves.
    if (actionWhenDone != null) {
      actionWhenDone();
    }
  }
}

function justClicked(robotColor) {
  mostRecentlyClickedRobotColor = robotColor;
}

function writeMessage(message) {
  document.getElementById("message").innerHTML = message;
}

function clearLatestChangesMessages() {
  document.getElementById("latestChangesMessages").innerHTML = "";
}

function displayLatestChanges(message) {
  var date = new Date();
  var dateString = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();

  var currentMessage = document.getElementById("latestChangesMessages").innerHTML;
  document.getElementById("latestChangesMessages").innerHTML = currentMessage + "<br>" + dateString + "  " + message;
}

function getPositionOnCanvas(canvas, domEvent) {
  var rect = canvas.getBoundingClientRect();
  var scaleX = canvas.width / rect.width;
  var scaleY = canvas.height / rect.height;

  return {
    x : (domEvent.clientX - rect.left) * scaleX,
    y : (domEvent.clientY - rect.top) * scaleY
  }
}

function cellXToX(cellX) {
  return (cellX * CELL_WIDTH);
}

function cellYToY(cellY) {
  return (cellY * CELL_HEIGHT);
}

function xToCellX(x) {
  return (x / CELL_WIDTH)
}

function yToCellY(y) {
  return (y / CELL_HEIGHT)
}

function getDirectionOfPositionRelativeToRobot(pos, robot) {
  var posX = pos.x;
  var posY = pos.y;
  var direction = null;
  if ((posX > robot.offsetLeft) && (posY - robot.offsetTop < CELL_HEIGHT)
      && (posY - robot.offsetTop > 0)) {
    direction = 'East';
  }
  if ((posY > robot.offsetTop) && (posX - robot.offsetLeft < CELL_WIDTH)
      && (posX - robot.offsetLeft > 0)) {
    direction = 'South';
  }
  if ((posY < robot.offsetTop) && (posX - robot.offsetLeft < CELL_WIDTH)
      && (posX - robot.offsetLeft > 0)) {
    direction = 'North';
  }
  if ((posX < robot.offsetLeft) && (posY - robot.offsetTop < CELL_HEIGHT)
      && (posY - robot.offsetTop > 0)) {
    direction = 'West';
  }
  return direction;
}

function getRobotFromColor(color) {
  var robot;
  switch (color) {
  case "Blue":
    robot = document.getElementById("blueRobot");
    break;
  case "Red":
    robot = document.getElementById("redRobot");
    break;
  case "Yellow":
    robot = document.getElementById("yellowRobot");
    break;
  case "Green":
    robot = document.getElementById("greenRobot");
    break;
  }
  return robot;
}

function getColorFromRobot(robot) {
  var color;
  switch (robot.id) {
  case "blueRobot":
    color = "Blue";
    break;
  case "redRobot":
    color = "Red";
    break;
  case "yellowRobot":
    color = "Yellow";
    break;
  case "greenRobot":
    color = "Green";
    break;
  }
  return color;
}

function updatePlayersAndScoresList(playersAndScores){
  var playerTable = document.getElementById("playerIds");
  console.log("player ids")
  var playerTableRows = playerTable.rows.length;
  console.log("player table rows" + playerTableRows)
  for (var i = 1; i < playerTableRows; i++){
    playerTable.deleteRow(-1);
  }
  for (var i = 0; i < playersAndScores.length; i++){
    var row = playerTable.insertRow(i + 1);
    console.log(row);
    var player = playersAndScores[i].playerId;
    console.log(player);
    var score = playersAndScores[i].score;
    console.log(score);
    row.innerHTML = player + ": " + score;
  }
}

function addGuesses(playersAndGuesses){
  var guessTable = document.getElementById("guesses");
  var guessTableRows = guessTable.rows.length;
  for (var i = 1; i < guessTableRows; i ++){
    guessTable.deleteRow(-1);
  }
  for (var i = 0; i < playersAndGuesses.length; i ++){
    var row = guessTable.insertRow(i + 1);
    var player = playersAndGuesses[i].playerId;
    var guess = playersAndGuesses[i].guess;
    row.innerHTML = player + ": " + guess;
  }
}

function displayTime(value){
 
  contextTime.clearRect(0, 0, canvasTime.width, canvasTime.height);

  
  contextTime.fillStyle = "red";
  contextTime.textAlign = "center";
  contextTime.font = "80px Verdana";
  
  if (value < 1000){
    return;
  } else if (value < 10000){
    contextTime.fillText(value.toString().substring(0,1), (canvasTime.width)/2, (canvasTime.height)/2);
  } else {
    contextTime.fillText(value.toString().substring(0,2), (canvasTime.width)/2, (canvasTime.height)/2);
  }
}

function showPlayerToMove(playerToMove){
  document.getElementById("playerToMove").innerHTML = playerToMove;
  playerMoves = 0;
}

function createJoinGameButtons(){
  for(var i = 0; i < gameIdsForLobby.length; i++){
    document.getElementById("joinGameButtons").innerHTML += "</br> <button id=\"joinGame" + gameIdsForLobby[i] + "\"" +
      "onclick=\"startGame(" + gameIdsForLobby[i] + ")\"> Join Game " + gameIdsForLobby[i] + "</button>";
  }
}

function updateMoves(moves){
  
}

