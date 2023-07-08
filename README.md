# tableworld

<img src="tw.jpg" width="600">

![build](https://github.com/eigenhombre/tableworld/actions/workflows/build.yml/badge.svg)

A little [MUD](https://en.wikipedia.org/wiki/Multi-user_dungeon)- and
[MUDDL](https://github.com/PDP-10/MUD1)-[inspired](https://if50.substack.com/p/1980-mud)
game.

# try it out!

    telnet 206.189.225.15 9999

(May disappear or be restarted without warning....)

# building it for yourself

Install [Leiningen](https://leiningen.org/) and `make` using your
package manager.  Then,

    make

# running a local copy

    ./tw

# playing your local copy

    telnet 127.0.0.1 9999

## example

    $ telnet 127.0.0.1 9999
    Trying 127.0.0.1...
    Connected to localhost.
    Escape character is '^]'.
    .....:::::........................................
    .......:^^:....................................... T
    ......::::^::..................................... A
    ......::::^...:................................... B
    .........^^::..................................... L
    .........~:............^!~:....................... E
    ........^P:.......:^75#&@@&BY~:................... W
    ........?57...:~?G&@@@@@@@@@@@&B57^:.............. O
    ........~7!!YB&@@@@@@@&~~~7&@@@@@@@#P?~:.......... R
    ......^?G&@@@@@@@@@@@@B...^G@@@@@@@@@@@&GJ~:...... L
    .....^P@@@@@@@@@@@@@@@B!7?PG&@@@@@@@&&&@@@G!...... D
    .:~^^~J@@@B&@@@@@@@@@&@@@@@&@@@@@@@@@&GG&@G.......
    :~!~~75#&#5&@&@@@@@@@@@@@@@&&#@@@&&@@@&&@@&^......
    :^^^^?5@@#5&@#&&&#&&&&&&&&@@@@@#&&&&&BPG#&@~.:::.:
    !!^!~JP&&#P&&#&&&####&###&@@@@@#BBPBB&5!YP#?~!7~^~
    &#B##GG&@&#&&&&####&#&#BB@@@@@@###PBB#B~!7?55557Y5
    @@@&&&BG&&GPPBBBGGPGGPPYY@@@@@@GG#GB###PPPYYGG5?GG
    BGGB&@BB#BJ7??JJY????7~!J&&&&&&5!?Y5B##&&@&&&&@&@@
    &#&@@@&J~^~~!~~~~~~!~~^~~!!~~~~~~~?5BBB&##&#GG#&&&
    @@@@@@@B7^^!!~~^^^^^^^^^^^^^^^^^^^~?J?YBGGP5P&@&&#
    @@@@@@@&YYJY~^^~^~~~~^^^^^:^^^^^^^~~~!??JJ?Y&@&&&&
             https://github.com/eigenhombre/tableworld

      Welcome to the world.

    What is your name? adsf adsf
    Sorry, no whitespace in player names (for now).
    What is your name? John
    Welcome to Table World, John.
      You are in the hearth room.  There is a heavy
      stone table here near a great fireplace lined
      with glazed red tile.  To the south lies the mud
      room.
    >>> s
      Mudroom.
    >>> look
      You are in the mud room.  Through the outside
      door to the south you see a great wood.  The
      doorway on the north side opens to the hearth.
    >>> s
      The great forest.
    >>> look
      You are in the woods.  Trunks and heavy
      overgrowth obscure your view in all directions.
      Just to the north is a small hut, with door
      ajar.
    >>> help
      Available: look help hello time dump quit
      n s e w north south east west
    >>> e
      You cannot go that way.
    >>> hello
      Hello, John!
    >>> quit
    Connection closed by foreign host.
    $

# license

MIT.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
