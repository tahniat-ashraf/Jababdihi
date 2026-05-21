type FeedPlaceholderProps = {
  eyebrow: string;
  title: string;
  description: string;
};

export function FeedPlaceholder({
  eyebrow,
  title,
  description
}: FeedPlaceholderProps) {
  return (
    <section className="max-w-3xl">
      <p className="text-sm font-medium uppercase tracking-wide text-muted-foreground">
        {eyebrow}
      </p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight">{title}</h1>
      <p className="mt-4 text-base leading-7 text-muted-foreground">
        {description}
      </p>
      <div className="mt-8 grid gap-4 sm:grid-cols-2">
        <div className="rounded-md border bg-card p-5 text-card-foreground">
          <h2 className="text-base font-semibold">Feed placeholder</h2>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            Incident cards, filters, source chips, and confidence indicators
            will be added after the API contract is implemented.
          </p>
        </div>
        <div className="rounded-md border bg-card p-5 text-card-foreground">
          <h2 className="text-base font-semibold">Local state only</h2>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            The actor switch and language links are present as UI skeletons
            without backend integration.
          </p>
        </div>
      </div>
    </section>
  );
}
