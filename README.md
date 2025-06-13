# Promises Discord Bot
A simple bot for messaging discord users with a promise from the Bible and maybe some study.
The configuration and the database should be in the same path as the JAR or in the same directory as your execution command.

## Bot Token
Put `PROMISES_DISCORD_BOT_TOKEN` as environment variable.

- **Server Members Intent** is required. *(Check discord developer portal)*

## Playlists
`/playlists` folder will contain the **playlists**. Each playlist is inside a folder and that folder's name is the playlist's name.

## Database
`users.sqlite` will contain the **subscribed** members, their timezones and their **subscribed** services.

## Promises
`/promises` will contain the promises in **JSON** format. Each promise has its own single file.

### Format for promises

A file name `name_of_file.json`

**The author if any**
- `"author": "Daily Promise"`

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

## Reminders

`reminder.json` is the configuration to all **reminders** and follows the same format/pattern as the promises.
It doesn't have to be in a specific folder, but as the database is, so is this file.

## Admins

Admins get DMs from the bot for their executed admin commands. In that way you can't hide anything and you don't expose something.

- `!reload` - Reloads the promises and reminder.

- `!admins` - Shows a list of admins with their names and IDs. It may send more than one embedded message if there are many.

- `!add_admin [user ID]` - Add this user (if it can be found) to the admins.

- `!remove_admin [user ID]` - Removes that admin if it is found in the database.
