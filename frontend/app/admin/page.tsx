export default function AdminPage() {
  return (
    <section className="max-w-3xl">
      <p className="text-sm font-medium uppercase tracking-wide text-muted-foreground">
        Admin
      </p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight">
        Pending incident review
      </h1>
      <p className="mt-4 text-base leading-7 text-muted-foreground">
        The admin workspace will list pending incidents for human review in a
        later phase. Authentication and internal API integration are not
        implemented yet.
      </p>
    </section>
  );
}
