# PromisesDiscordBot
A simple bot for messaging discord userEntities with a promise from the Bible and maybe some study.
The configuration and the database should be in the same path as the JAR.

## Playlists
`/playlists` folder will contain the playlists. Each playlist is inside a folder and that folder's name is the playlist's name, which should be inside that `/playlists` folder.

## Database
`userEntities.sqlite` will contain the subscribed members and their timezones.

## Promises
`/promises` will contain the promises in JSON format. Each promise has it's own single file.

### Format for promises

A file name `name_of_file.json`

**The author if any**
- `"author": "John"`

**The footer of the message**
- `"footer": "File 1.json"`

**The color of the embed on the left side**
- `"color": "green"`

**Url for references or resources**

*Make sure to provide a valid url*

- `"url": "https://google.com/..."`

**Url for the image if any**

*Make sure to provide a valid url*

- `"image": "https://google.com/..."`

**All the fields for the message.**

*It is a list like that []*
- `"fields: [...]`

**Add a field to the message**

*The "inline" option is how you want to display that field. By default, it will be **false***
```json
{
  "name": "The title of the field. It will be automatically bold",
  "value": "The text for that field. It can be examples or explanation, or a verse.",
  "inline": false
}
```

**NOTE: Please remember to put the `,` at the end of each line except the last line.
Otherwise, the bot can't read the invalid JSON format.**

**Example:**

```json
{
  "author": "Daily Promise",
  "footer": "File 1.json",
  "url": "",
  "image": "",

  "fields": [
    {
      "name": "John 3:16-17",
      "value": "16 For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.\n\n17 For God sent not his Son into the world to condemn the world; but that the world through him might be saved.",
      "inline": false
    },

    {
      "name": "The promise of eternal life and salvation",
      "value": "These two verses shows us the promise that we have, when we follow and believe on Jesus Christ. That promise is to have eternal life in Heaven with God forever."
    }
  ]
}
```

## Bot Token
Put `DISCORD_BOT_TOKEN` as environment variable.

