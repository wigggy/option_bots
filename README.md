<h3> Notes </h3>
You will need to create a file called 'cs_auth.json' that looks like the following, 
and add it to the 'bots' module '/resources' folder, OR in the 'bots' module
go to the 'main/kotlin/com.github.wi110r.botsbase/systems/Common.kt' and modify the
initCsApi() function to read the file from another path.
The app depends on this for Charles Schwab Api Authentication system. 


```json
{
  "key": "your-app-key",
  "secret": "your-app-secret"
}
```