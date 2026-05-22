export default function MethodologyPage() {
  return (
    <article className="mx-auto w-full max-w-2xl py-6">
      <p className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
        Methodology and legal notes
      </p>
      <h1 className="mt-2 font-serif text-[2rem] font-medium leading-tight tracking-[-0.025em] text-foreground">
        How Jababdihi handles public reports
      </h1>

      <section className="mt-6 space-y-3 font-serif text-[1rem] leading-relaxed text-foreground-soft">
        <p>
          Jababdihi is an evidence-first public feed. It aggregates reports from
          allowlisted newspapers and official sources, preserves direct source
          links, and separates published incidents from items still awaiting
          review.
        </p>
        <p>
          Incident summaries use neutral language such as reported, alleged, and
          sources reported. The system does not determine legal truth, guilt, or
          liability.
        </p>
      </section>

      <section className="mt-8">
        <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
          Confidence
        </h2>
        <p className="mt-2 font-serif text-[1rem] leading-relaxed text-foreground-soft">
          Confidence reflects source corroboration strength. It is calculated
          from source count, independent publisher count, and metadata
          consistency. It is not a legal finding.
        </p>
      </section>

      <section className="mt-8" id="legal">
        <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
          Legal disclaimer
        </h2>
        <p className="mt-2 border-l-2 border-foreground bg-paper px-4 py-3 font-serif text-[0.95rem] italic leading-relaxed text-foreground-soft">
          This platform aggregates publicly reported incidents and allegations
          from listed sources. Confidence scores represent source corroboration
          strength, not legal proof, guilt, or a court finding.
        </p>
      </section>
    </article>
  );
}
