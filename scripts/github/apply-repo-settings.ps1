<#
.SYNOPSIS
  Applies the versioned GitHub settings and rulesets of brasil4j (GF-6, VII.3).

.DESCRIPTION
  Reads .github/rulesets/*.json and creates or updates each ruleset by name, then sets the
  repository merge options. Without -Apply it only prints what it would do. Requires the GitHub
  CLI authenticated with admin rights on the repository (token from the environment; never stored).

.EXAMPLE
  pwsh scripts/github/apply-repo-settings.ps1            # dry run
  pwsh scripts/github/apply-repo-settings.ps1 -Apply     # apply
#>
[CmdletBinding()]
param(
    [string]$Repo = 'KrystianYCSilva/brasil4j',
    [string]$DefaultBranch = 'develop',
    [switch]$Apply
)

$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..' '..')
$rulesetDir = Join-Path $root '.github' 'rulesets'

function Invoke-Gh {
    param([string[]]$Arguments, [string]$Body)
    if (-not $Apply) {
        Write-Host "[dry-run] gh $($Arguments -join ' ')"
        return $null
    }
    if ($Body) {
        $tmp = New-TemporaryFile
        try {
            Set-Content -Path $tmp -Value $Body -Encoding utf8NoBOM
            $out = & gh @Arguments --input $tmp
        } finally {
            Remove-Item $tmp -Force
        }
    } else {
        $out = & gh @Arguments
    }
    if ($LASTEXITCODE -ne 0) { throw "gh $($Arguments -join ' ') failed with exit code $LASTEXITCODE" }
    return $out
}

& gh api "repos/$Repo/branches/$DefaultBranch" --silent 2>$null
if ($LASTEXITCODE -ne 0) {
    throw "Branch '$DefaultBranch' does not exist on $Repo; push it before applying settings."
}

$settings = @{
    default_branch          = $DefaultBranch
    delete_branch_on_merge  = $false
    allow_auto_merge        = $true
    allow_update_branch     = $true
    allow_merge_commit      = $true
    allow_squash_merge      = $true
    allow_rebase_merge      = $false
} | ConvertTo-Json -Compress
Invoke-Gh -Arguments @('api', '-X', 'PATCH', "repos/$Repo") -Body $settings | Out-Null
Write-Host "Repository settings: $settings"

$existing = @{}
foreach ($line in (& gh api "repos/$Repo/rulesets" --jq '.[] | "\(.id)\t\(.name)"')) {
    if ($line) {
        $parts = $line -split "`t"
        $existing[$parts[1]] = $parts[0]
    }
}

foreach ($file in Get-ChildItem -Path $rulesetDir -Filter '*.json' | Sort-Object Name) {
    $body = Get-Content -Raw -Path $file.FullName
    $name = ($body | ConvertFrom-Json).name
    if ($existing.ContainsKey($name)) {
        Invoke-Gh -Arguments @('api', '-X', 'PUT', "repos/$Repo/rulesets/$($existing[$name])") -Body $body | Out-Null
        Write-Host "$(if ($Apply) { 'Updated' } else { 'Would update' }) ruleset '$name' from $($file.Name)"
    } else {
        Invoke-Gh -Arguments @('api', '-X', 'POST', "repos/$Repo/rulesets") -Body $body | Out-Null
        Write-Host "$(if ($Apply) { 'Created' } else { 'Would create' }) ruleset '$name' from $($file.Name)"
    }
}

if (-not $Apply) { Write-Host 'Dry run only. Re-run with -Apply to change the repository.' }
