# VoicLog

A quick voice-journaling app for capturing what you got done in a day.

Doing the work is easy — remembering it later isn't. VoicLog takes 1-2 minutes: hit record, talk through your day, and it turns that into simple, concise bullet points. Over a week, those add up into a summary you can look back on.

## Why offline

VoicLog is strictly offline. Speech-to-text and summarization both run on-device via local models — your recordings and journal entries never leave your phone.

## How it works

- **Record** — tap the mic on the home screen and talk. The home screen also shows a quick sense of progress (e.g. "4 Weeks Logged") and a one-liner for last week.
- **Log** — a weekly view of consolidated entries, with more detail available per week.
- **Settings** — see (and eventually configure) the on-device models powering transcription and summarization.

Since the on-device models need to be downloaded on first launch, that download is folded into onboarding rather than being a separate setup step.

## Tech stack

- Kotlin, Jetpack Compose, Coroutines/Flow, Dagger (Hilt)
- Whisper for speech-to-text
- Gemma for summarization
- Room for local persistence

## Architecture

MVI-oriented: the UI and ViewModels stay agnostic of which on-device model is doing the work — that's abstracted behind a repository layer that talks to Room and coordinates the model pipeline.
