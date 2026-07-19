use poke_engine::mcts::perform_mcts;
use poke_engine::state::State;
use std::time::Duration;

// Required once per crate when using proc-macro-only (no .udl file) mode.
uniffi::setup_scaffolding!();

#[derive(uniffi::Record)]
pub struct MoveResult {
    pub move_choice: String,
    pub average_score: f32,
    pub visits: i64,
}

#[derive(uniffi::Record)]
pub struct SearchResult {
    pub side_one: Vec<MoveResult>,
    pub side_two: Vec<MoveResult>,
    pub iterations: i64,
}

/// Runs poke-engine's decoupled-UCT MCTS from a serialized state string and
/// returns per-move average score + visit counts for BOTH sides (side_two's
/// numbers are informative but you'll generally only act on side_one's,
/// unless you're using this to also model what the opponent is likely to do).
///
/// - `time_ms`: soft time budget for the search.
/// - `iterations`: if nonzero, search this many iterations instead of by
///   time (time_ms is ignored in that case). Pass 0 to search by time.
#[uniffi::export]
pub fn best_move_mcts(state_str: String, time_ms: u64, iterations: u32) -> SearchResult {
    let mut state = State::deserialize(&state_str);
    let (s1_options, s2_options) = state.root_get_all_options();

    let result = perform_mcts(
        &mut state,
        s1_options,
        s2_options,
        Duration::from_millis(time_ms),
        iterations,
    );

    let side_one = result
        .s1
        .iter()
        .map(|r| MoveResult {
            move_choice: r.move_choice.to_string(&state.side_one),
            average_score: r.average_score(),
            visits: r.visits as i64,
        })
        .collect();

    let side_two = result
        .s2
        .iter()
        .map(|r| MoveResult {
            move_choice: r.move_choice.to_string(&state.side_two),
            average_score: r.average_score(),
            visits: r.visits as i64,
        })
        .collect();

    SearchResult {
        side_one,
        side_two,
        iterations: result.iteration_count as i64,
    }
}

/// Parses `state_str` and immediately re-serializes it. Use this from Kotlin
/// while you're building your state-string converter: if your hand-built
/// string doesn't round-trip cleanly (or poke-engine panics on it), you know
/// your serialization is wrong before you ever get to debugging a search
/// result that just looks "off".
#[uniffi::export]
pub fn debug_roundtrip_state(state_str: String) -> String {
    State::deserialize(&state_str).serialize()
}

/// Lists the legal moves/switches for both sides as poke-engine sees them,
/// using the same move-name strings best_move_mcts returns. Handy for
/// sanity-checking that your state string produced the move set you expected.
#[uniffi::export]
pub fn legal_options(state_str: String) -> Vec<Vec<String>> {
    let state = State::deserialize(&state_str);
    let (s1_options, s2_options) = state.root_get_all_options();
    vec![
        s1_options
            .iter()
            .map(|m| m.to_string(&state.side_one))
            .collect(),
        s2_options
            .iter()
            .map(|m| m.to_string(&state.side_two))
            .collect(),
    ]
}

// basic test case
#[uniffi::export]
pub fn test_connection() -> String {
    "Hello from Rust MegaGliscor engine!".to_string()
}