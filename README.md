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

Install Java, Leiningen, make.  Then,

    make

# running a local copy

    ./tw

# playing your local copy

    telnet 127.0.0.1 9999

## example

    $ telnet 127.0.0.1 9999
    Trying 127.0.0.1...
    Connected to localhost.
    What is your name? John
    Welcome to Table World, John.
    >>> help
    Available: look help hello time dump quit
    >>> hello
    Hello, John!
    >>> look
    You are in the hearth room.  There is a heavy stone table here
    near a great fireplace lined with glazed red tile.  To the south
    lies the mud room.
    >>> dump
    {:rooms
     {"hearth"
      {:id "hearth",
       :shortdesc "The hearth.",
       :desc
       "You are in the hearth room.  There is a heavy stone table here
       near a great fireplace lined with glazed red tile.  To the
       south lies the mud room."},
      "mudroom"
      {:id "mudroom",
       :shortdesc "Mudroom.",
       :desc
       "You are in the mud room.  Through the outside door to the
       south you see a great wood.  The doorway on the north side
       opens to the hearth."},
      "forest"
      {:id "forest",
       :shortdesc "The great forest.",
       :desc
       "You are in the woods.  Trunks and heavy overgrowth obscure
       your view in all directions.  Just to the north is a small hut,
       with door ajar."}},
     :map
     ({:id "hearth", :neighbors {:s "mudroom"}}
      {:id "mudroom", :neighbors {:n "hearth", :s "forest"}}
      {:id "forest", :neighbors {:n "mudroom"}}),
     :players {"John" {:location "hearth", :name "John"}}}
    nil
    >>> ^D
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
