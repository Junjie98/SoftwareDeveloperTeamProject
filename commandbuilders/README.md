# Cheatsheet for CommandBuilders

> **Disclaimer**
>
> This code is in no way stable. Some method hasn't been implemented/developed in a testable environment, so sample code labelled *expected* may contain errors or bugs that prevents it from working properly.
>
> The sample code and the implementation details may be changed along the way during development.

## CardInHandCommandBuilder

This builder sends out specified `DRAW` or `DELETE` command.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will first need to use `.setMode(CardInHandCommandMode)` to decide the mode to be used. The description is as following:

* mode `DRAW` - You will need to call `.setCard(Card)`, `.setPosition(int)`, `.setState(States)` to set the card to be added, the position of the card in the hand, and the `NORMAL` or `HIGHLIGHTED` state of the card before called `.issueCommand()`. Card state is defined by `States`, including `HIGHLIGHTED`, `NORMAL`.
* mode `DELETE` - When set to delete, you will need to call .setPosition(int) to indicate the position of card to be removed. Notice any other fields set will be ignored by the `.issueCommand()` function.

The following code draws card at `currentCardInHand` array's `idx` at position `idx`.

```java
new CardInHandCommandBuilder(out)
        .setCommandMode(CardInHandCommandMode.DRAW)
        .setCard(currentCardInHand[idx])
        .setPosition(idx)
        .setState(States.NORMAL)
        .issueCommand();
```

The following code removes card from position `idx`.

```java
new CardInHandCommandBuilder(out)
        .setCommandMode(CardInHandCommandMode.DELETE)
        .setPosition(idx)
        .issueCommand();
```

## PlayerNotificationCommandBuilder

This builder sends out Notification in the specified seconds.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will need to call `.setMessage(String)` to set the message, `.setDisplaySeconds(int)` to set the time to be displayed, and `.setPlayer(Players)` before calling `.issueCommand()`.

Although the user is defined as enum of PLAYER1 and PLAYER2, the PLAYER2 is not properly supported by the front end so it is strongly discouraged to use it.

The following code notifies Player 1 of his/her turn:

```java
new PlayerNotificationCommandBuilder(out)
		.setMessage("Player 1's turn")
		.setDisplaySeconds(2)
		.setPlayer(Players.PLAYER1)
		.issueCommand();
```

## PlayerSetCommandsBuilder

This builder sends out commands to set stats of the `Player`.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will need to call `.setPlayer(Players)` to decide the player to set is `PLAYER1` or `PLAYER2`, `.setStats(PlayerStats)` to decide to set `MANA`, `HEALTH`, or `ALL`, and `.setInstance(Player)` to provide an instance whose specified Stats will be set to the UI.

The following code generates a new user and set all the stats to the UI for Player 1:

```java
 Player player1 = new Player();
 new PlayerSetCommandsBuilder(out)
        .setPlayer(Players.PLAYER1)
        .setStats(PlayerStats.ALL)
        .setInstance(player1)
        .issueCommand();
```

## ProjectTileAnimationCommandBuilder

This builder is to display a ProjectTile animation.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will need to call `.setUnit1(Unit, Tile)` and `.setUnit2(Unit, Tile)` before you issue the command.

Notice the issueCommand code is now a generalised version of the Demo code. Further testing may reveal some refinements needed.

This code is *expected* to be called like:

```java
// definition of unit 1 / tile 1
// definition of unit 2 / tile 2

new ProjectTileAnimationCommandBuilder(out)
		.setUnit1(unit1, tile1)
		.setUnit2(unit2, tile2)
		.issueCommand();
```

## TileCommandBuilder

This class can be used to send out `DRAW` or `ANIMATION` commands for `Tiles`.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will need to specified the mode with `.setMode(TileCommandBuilderMode)`, with the following descriptions:

You will need to call `.setX(int)` and `.setY(int)` to specify the position of the tile.

* mode `DRAW`: Use `.setState(States)` to decide `NORMAL` or `HIGHLIGHTED` state of the command.

* mode `ANIMATION`: You will need to call `.setEffectAnimation(TileEffectAnimation)` to set animation. It is defined with an enum containing `INMOLATION`, `BUFF`, `MARTYRDOM`, `SUMMON` options.

Drawing a tile of state `NORMAL` at (`idx`, `jdx`) will look like:

```java
new TileCommandBuilder(out)
		.setX(idx).setY(jdx).setState(States.NORMAL)
		.issueCommand();
```

Drawing an animation on the tile is *expected* to be called like:

```java
new TileCommandBuilder(out)
		.setX(idx).setY(jdx)
		.setEffectAnimation(TileEffectAnimation.BUFF)
		.issueCommand();
```

## UnitCommandBuilder

This command builder is the most flexible command builder, with modes: `DRAW`, `MOVE`, `SET`, `DELETE`, `ANIMATION`.

The initialisation step takes the `ActorRef` that is used to send/receive commands in the front-end side.

You will first need to specify the mode by calling `.setMode(UnitCommandBuilderMode)`.

You will need to specify a unit by calling `.setUnit(Unit)`.

For `MOVE` and `DRAW` commands, just specify `.setTile(Tile)` to specify the target position.

For setting stats of a unit, use `.setStats(UnitStats, int)`, to set the `ATTACK` or `HEALTH` to the following value.

For `ANIMATION`, specify one of the `UnitAnimationType` by calling `.setAnimationType(UnitAnimationType)`, to specify one of `idle`, `death`, `attack`, `move`, `channel`, `hit`.

Each command issuing will only take effect once. Setting the non-required fields will be ignored.

Sedning a draw command will be *expected* to be like this:

```java
new UnitCommandBuilder(out)
		.setMode(UnitCommandBuilderMode.DRAW)
		.setTile(tile)
		.issueCommand();
```

Sending a set stats command will be *expected* to be like this:

```java
new UnitCommandBuilder(out)
		.setMode(UnitCommandBuilderMode.SET)
		.setStats(UnitStats.ATTACK, 20)
		.issueCommand();
```

Sending an animaton command will be *expected* to be like this:

```java
new UnitCommandBuilder(out)
		.setMode(UnitCommandBuilderMode.ANIMATION)
		.setAnimationType(UnitAnimationType.attack)
		.issueCommand();
```