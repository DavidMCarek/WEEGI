$script:ip = "0.0.0.1"

function find {
	$script:ip = $([System.Uri]$($(New-Object -ComObject UPnP.UPnPDeviceFinder).FindByType("urn:schemas-upnp-org:device:Basic:1", 0) | % {$_.PresentationURL})).Host
	Write-Host('$script:ip=' + $script:ip)
}

function attach {
	Write-Host('POST /command {"command":"{"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'{'}"
}

function starts {
	Write-Host('GET /stream/start')
	Invoke-WebRequest -Uri http://$script:ip/stream/start -Method GET
}

function open {
	Write-Host('POST /command {"command":"!@#$%^&*"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'!@#$%^&*'}"
}

function startr {
	Write-Host('POST /command {"command":"A"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'A'}"
}

function stopr {
	Write-Host('POST /command {"command":"j"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'j'}"
}

function close {
	Write-Host('POST /command {"command":"12345678"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'12345678'}"
}

function stops {
	Write-Host('GET /stream/stop')
	Invoke-WebRequest -Uri http://$script:ip/stream/stop -Method GET
}

function detach {
	Write-Host('POST /command {"command":"}"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'}'}"
}

function outj {
	Write-Host('GET /output/json')
Invoke-WebRequest -Uri http://$script:ip/output/json -Method GET
}

function outr {
	Write-Host('GET /output/raw')
Invoke-WebRequest -Uri http://$script:ip/output/raw -Method GET
}

function mqtt {
param(
	[Parameter(Mandatory=$true)]
	[string]$broker,
	[string]$port,
	[string]$username,
	[string]$password
	)
	$Body = "{'broker_address':'$broker'"
	if ($PSBoundParameters.ContainsKey('port')) {
		$Body += ", 'port':$port"
	}
	if ($PSBoundParameters.ContainsKey('username')) {
		$Body += ", 'username':'$username'"
	}
	if ($PSBoundParameters.ContainsKey('password')) {
		$Body += ", 'password':'$password'"
	}
	$Body += "}"
	$PSBoundParameters.ContainsKey('IsValueNameRegularExpression')
	
	Write-Host("POST /mqtt {""$Body""}")
	Invoke-WebRequest -Uri http://$script:ip/mqtt -Method POST -ContentType "application/json" -Body "$Body"
}

function command {
param(
	[string]$command
	)
	Write-Host('POST /command {"command":"' + $command + '"}')
	Invoke-WebRequest -Uri http://$script:ip/command -Method POST -ContentType "application/json" -Body "{'command':'$command'}"
}

function get {
param(
	[Parameter(Mandatory=$true)]
	[string]$path
	)
	Write-Host('GET /' + $path)
	Invoke-WebRequest -Uri http://$script:ip/$path -Method GET
}

function command {
param(
	[Parameter(Mandatory=$true)]
	[string]$path,
	[string]$body
	)
	Write-Host('POST /command {"command":"' + $command + '"}')
	if ($PSBoundParameters.ContainsKey('body')) {
		Invoke-WebRequest -Uri http://$script:ip/$path -Method POST -ContentType "application/json" -Body "$body"
	} else {
		Invoke-WebRequest -Uri http://$script:ip/$path -Method POST
	}
}

function delete {
param(
	[Parameter(Mandatory=$true)]
	[string]$path
	)
	Write-Host('DELETE /' + $path)
	Invoke-WebRequest -Uri http://$script:ip/$path -Method DELETE
}