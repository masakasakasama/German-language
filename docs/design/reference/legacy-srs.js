// Spaced repetition based on the Ebbinghaus forgetting curve.
//
// Hermann Ebbinghaus (1885) showed that without rehearsal, recall drops to
// roughly 60% within 20 minutes, 35% within 9 hours, and 20% within 31 days,
// while each successful retrieval flattens the curve and pushes the next
// review further out. Piotr Wozniak's SuperMemo SM-2 (1985) operationalised
// this with expanding intervals; Anki uses the same schedule.
//
// Default progression for a confident answer ("good") here:
//   reps 0 → 1 day      (just learned; recall is most fragile)
//   reps 1 → 3 days     (covers the 24h-72h forgetting cliff)
//   reps 2 → 7 days     (one-week consolidation)
//   reps 3 → 14 days
//   reps 4+ → previous interval × ease factor
//
// Mark-driven adjustments (set from Browse/Lesson "Easy / OK / Hard"):
//   Hard ("dontknow") → interval = 1 day, reps reset, ease −0.20.
//                       Card stays in the short-cycle until you stop missing it.
//   OK ("ok")         → treated as a normal "good" review.
//   Easy ("know")     → jumps 2 boxes ahead (≥ 4 days, often 14+), ease +0.15.
//
// "Again" wrong answer mid-quiz is more aggressive: interval = 0 (today).
window.SRS = (function () {
  const MIN_EASE = 1.3;
  const DEFAULT_EASE = 2.5;
  // Ebbinghaus-aligned graduating intervals (days)
  const STEPS = [1, 3, 7, 14];

  function todayDate() {
    const d = new Date();
    d.setHours(0,0,0,0);
    return d;
  }
  function addDays(d, n) {
    const r = new Date(d);
    r.setDate(r.getDate() + n);
    return r;
  }

  function defaultState() {
    return {
      ease: DEFAULT_EASE,
      interval: 0,
      due: todayDate().toISOString(),
      reps: 0,
      lapses: 0,
      last: null
    };
  }

  // Update card after review.
  // grade: "again" | "hard" | "good" | "easy"
  function review(state, grade) {
    state = state || defaultState();
    let { ease, interval, reps, lapses } = state;

    if (grade === "again") {
      interval = 0;
      ease = Math.max(MIN_EASE, ease - 0.2);
      lapses += 1;
      reps = 0;
    } else if (grade === "hard") {
      ease = Math.max(MIN_EASE, ease - 0.15);
      // stay in graduating steps but advance slowly
      const step = Math.min(reps, STEPS.length - 1);
      interval = STEPS[step];
      reps += 1;
    } else if (grade === "good") {
      // Ebbinghaus-aligned graduating steps for the first few reviews,
      // then exponential expansion via ease factor.
      if (reps < STEPS.length) interval = STEPS[reps];
      else interval = Math.round(interval * ease);
      reps += 1;
    } else if (grade === "easy") {
      // Skip a step in the graduating sequence; bump ease.
      const step = Math.min(reps + 2, STEPS.length - 1);
      interval = reps + 2 < STEPS.length ? STEPS[step] : Math.round(interval * ease * 1.3);
      ease = Math.min(3.0, ease + 0.15);
      reps += 1;
    }

    const due = addDays(todayDate(), interval).toISOString();
    return {
      ease, interval, reps, lapses, due,
      last: new Date().toISOString()
    };
  }

  // Apply a self-assessment mark to the SRS schedule.
  //   "dontknow" (Hard)  → reset to 1-day interval, drop ease (so it stays short)
  //   "ok"               → treat as a normal "good" review
  //   "know" (Easy)      → push interval far out (>=7 days, double current),
  //                        raise ease
  function applyMark(state, mark) {
    state = state || defaultState();
    if (mark === "dontknow") {
      return {
        ease: Math.max(MIN_EASE, state.ease - 0.2),
        interval: 1,
        reps: state.reps || 0,
        lapses: (state.lapses || 0) + 1,
        due: addDays(todayDate(), 1).toISOString(),
        last: new Date().toISOString()
      };
    }
    if (mark === "know") {
      const newInterval = Math.max(7, Math.round((state.interval || 1) * 2));
      return {
        ease: Math.min(3.0, state.ease + 0.15),
        interval: newInterval,
        reps: (state.reps || 0) + 1,
        lapses: state.lapses || 0,
        due: addDays(todayDate(), newInterval).toISOString(),
        last: new Date().toISOString()
      };
    }
    if (mark === "ok") return review(state, "good");
    return state;
  }

  function isDue(state) {
    if (!state) return true;
    return new Date(state.due).getTime() <= todayDate().getTime();
  }

  // FSRS-style spread: jitter intervals so big batches don't all fall due on
  // the same day (keeps daily review load even). Short intervals untouched.
  function fuzz(interval) {
    if (interval < 4) return interval;
    const spread = Math.max(1, Math.round(interval * 0.05));
    return interval + (Math.floor(Math.random() * (spread * 2 + 1)) - spread);
  }

  // How "weak" a card is: higher = needs work. Driven by lapses (forgetting),
  // low ease (consistently hard), and being overdue.
  function weakness(state) {
    if (!state) return 0;
    let s = (state.lapses || 0) * 3;
    s += Math.max(0, (DEFAULT_EASE - (state.ease || DEFAULT_EASE))) * 4;
    if (state.interval != null && state.interval <= 1 && (state.reps || 0) > 0) s += 2;
    const overdueDays = Math.floor((todayDate().getTime() - new Date(state.due).getTime()) / 86400000);
    if (overdueDays > 0) s += Math.min(overdueDays, 7) * 0.5;
    return s;
  }

  // Cards the learner keeps getting wrong — for a focused "weak words"
  // session, independent of due date. Sorted hardest-first.
  function weakCardIds(allCards, lang, limit) {
    const scored = [];
    allCards.forEach((c) => {
      const st = Storage.getCard(c.id, lang);
      if (!st || (st.reps || 0) === 0) return;
      const w = weakness(st);
      if (w >= 3) scored.push({ id: c.id, w: w });
    });
    scored.sort((a, b) => b.w - a.w);
    return scored.slice(0, limit || 30).map((x) => x.id);
  }

  function countWeak(allCards, lang) {
    return weakCardIds(allCards, lang, 9999).length;
  }

  function dueCardIds(allCards, lang) {
    return allCards.filter((c) => {
      const st = Storage.getCard(c.id, lang);
      if (!st) return false;
      return isDue(st);
    }).map((c) => c.id);
  }

  function countDue(allCards, lang) {
    return allCards.reduce((acc, c) => {
      const st = Storage.getCard(c.id, lang);
      if (st && isDue(st)) acc += 1;
      return acc;
    }, 0);
  }

  function summary(state) {
    if (!state) return "new";
    if (state.interval === 0) return "learning";
    if (state.interval >= 21) return "mastered";
    return "review";
  }

  return {
    review, applyMark, isDue, dueCardIds, countDue, summary, defaultState,
    weakCardIds, countWeak, weakness, fuzz,
    DEFAULT_EASE, MIN_EASE
  };
})();
