# FireMind Architecture

FireMind is currently a native Android/Kotlin application targeting Fire OS. The app is deliberately TV-first: it uses a large landscape presentation, explicit focus styling, remote-friendly quick prompts, a curated catalog, and local persistence for the watchlist.

```text
Fire TV / Android TV validation target
        |
        | local deterministic recommendation path
        v
Native Android/Kotlin UI
        |
        | future HTTPS adapter using the same JSON contract
        v
FireMind backend API
        |
        | future secure provider adapter
        v
Amazon Bedrock or another authorized AI provider
```

The local catalog is the source of truth for title metadata. Recommendation logic ranks or filters catalog items and generates explanations from known fields. This prevents a model from inventing titles, runtimes, ratings, or descriptions. The Android app contains the same deterministic fallback path so network or model failure does not freeze the TV experience.

Amazon Devices Builder Tools and Vega-specific skills were used as design references for focus management, TV-safe spacing, performance awareness, and platform boundaries. Vega-only React Native packages were not added to this Fire OS Kotlin implementation.
